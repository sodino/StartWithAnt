package lab.sodino.sliding;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class CellLayout extends ViewGroup {
	/**动画恢复到起始或末尾位置花费的时间。*/
	public static final int SCROLL_DURATION = 300;
	/**单元格为正方形时，在480屏幕上宽度长占比。*/
	public static final double CELL_LENGTH_RATIO = 145d / 480;
	/**每一行默认单元格个数为3。*/
	public static final int COLUMN_COUNT_DEFAULT = 3;
	/** 单元格的宽高。此处先设定宽高相等。*/
	private int cellWidth, cellHeight;
	/**分隔线的宽高值。*/
	private int cellLineLength;
	/** 手指滑动过程中可理解为拖动的最小长度。 */
	private int distanceSlop;
	private static final int STATE_STATIC = 0;
	private static final int STATE_SCROLLING = 1;
	private int state;
	private Scroller scroller;
	private float lastMotionY;
	/**记录最近一次onLayout()时子组件的个数。<br/>
	 * 当子组件个数变化时，需要动态滑动至新一行或缩回一行。*/
	private int lastChildCount;
	
	public CellLayout(Context context) {
		super(context);
		init();
	}

	public CellLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		cellWidth = (int) (screenWidth * CELL_LENGTH_RATIO);
		cellHeight = cellWidth;
		cellLineLength = (screenWidth - cellWidth * COLUMN_COUNT_DEFAULT)/(COLUMN_COUNT_DEFAULT + 1);
		
		scroller = new Scroller(getContext());
		state = STATE_STATIC;
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		distanceSlop = configuration.getScaledTouchSlop();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogOut.out(this, "onMeasure()");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			int childWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY);
			int childHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY);
			View view = getChildAt(i);
			view.measure(childWidthSpec, childHeightSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int rowX = 0,columnY = 0;
		final int count = getChildCount();
		LogOut.out(this, "onLayout count=" + count +" changed="+changed);
		for (int i = 0; i < count; i++) {
			int quotient = i / COLUMN_COUNT_DEFAULT;
			int remainder = i % COLUMN_COUNT_DEFAULT;
			
			int cellLineX =  (remainder + 1) * cellLineLength;
			int cellLineY = (quotient + 1) * cellLineLength;
			
			rowX = remainder * cellWidth;
			columnY = quotient * cellHeight;
			final View view = getChildAt(i);
			if (view.getVisibility() != View.GONE) {
				int childWidth = view.getMeasuredWidth();
				int childHeight = view.getMeasuredHeight();
				view.layout(rowX + cellLineX, columnY + cellLineY, rowX + cellLineX + childWidth, columnY + cellLineY + childHeight);
			}else {
				LogOut.out(this, "layout... " + i);
			}
		}
		
		if(count > lastChildCount){
			// 新添加组件了 
			if(count % COLUMN_COUNT_DEFAULT == 1){
				// 新添加一行了
				LogOut.out(this, "new a line:count="+count);
				scroll2End();
			}
		}else if(count < lastChildCount){
			// 被删除了
			LogOut.out(this, "delete a item:count="+count +" Height="+getHeight() +" scrollY="+getScrollY() +" bottom="+(getChildAt(getChildCount() - 1).getBottom() + cellLineLength));
			if(getHeight() >= (getChildAt(getChildCount() - 1).getBottom() + cellLineLength) ){
				scroll2Head();
			}else if(getHeight() + getScrollY() >= (getChildAt(getChildCount() - 1).getBottom() + cellLineLength)){
				scroll2End();
			}
		}
		lastChildCount = count;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		LogOut.out(this, "onTouchEvent");
		super.onTouchEvent(event);
		final int action = event.getAction();
		final float y = event.getY();
		if(/*y < ||*/ (state == STATE_STATIC&&y > getChildAt(getChildCount() - 1).getBottom() + cellLineLength - getScrollY())){
			return true;
		}
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastMotionY = y;
			if (scroller.isFinished() == false) {
				scroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (state == STATE_STATIC) {
				checkScrolling(y);
			} else if (state == STATE_SCROLLING) {
				int moveY = (int) (lastMotionY - y);
				lastMotionY = y;
				int scrollY = getScrollY();
				LogOut.out(this, "Height="+getHeight() +" scrollY="+scrollY +" touchY="+ y +" bottom="+getChildAt(getChildCount() - 1).getBottom());
				if (scrollY < 0 || (getHeight() + scrollY)> getChildAt(getChildCount() - 1).getBottom() + cellLineLength) {
					// 对于越界的拖拉，则将位移减为原1/3。
					moveY = moveY / 3;
				}
				scrollBy(0, moveY);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			scroll2HeadOrEnd(y);
			state = STATE_STATIC;
			break;
		}
		return true;
	}
	
	private void scroll2HeadOrEnd(float touchY){
		final int bottomPointY =  getChildAt(getChildCount() -1).getBottom() + cellLineLength;
		final int nowY = getScrollY();
		int newY = 0;
		if (scroller.isFinished() == false) {
			return;
		}else if(nowY < 0){
			newY = 0;
		}else if(getHeight() > bottomPointY && nowY >= 0){
			// 如：当只有一行时，手指往上推，释放后，需要滑回第一行
			newY = 0;
		}else if(getHeight() + nowY > bottomPointY && getHeight() < bottomPointY){
			LogOut.out(this, "getHeight="+getHeight() +" nowY="+nowY +" sum="+(getHeight() + nowY)+ " to="+(getChildAt(getChildCount() -1).getBottom() + cellLineLength));
			newY = getChildAt(getChildCount() - 1).getBottom() + cellLineLength - getHeight();
		}else {
			// 在正常区域内滑动，无需在恢复到起始或终止位置
			return;
		}
		enableChildrenCache(true);

		View focusedChild = getFocusedChild();
		if (focusedChild != null) {
			focusedChild.clearFocus();
		}
		
		final int move = newY - nowY;
		final int absMove = Math.abs(move);
		int duration = SCROLL_DURATION;
		if (absMove < cellLineLength) {
			duration = SCROLL_DURATION * absMove / cellLineLength;
		}
		// 启动恢复动画
		scroller.startScroll(0, nowY, 0, move, duration);
		invalidate();
	}
	
	private void scroll2End(){
		final int nowY = getScrollY();
		final int newY = getChildAt(getChildCount() - 1).getBottom() + cellLineLength - getHeight();
		if(newY < 0){
			// 新增的行仍在视野范围内，不处理
			return;
		}
		enableChildrenCache(true);

		View focusedChild = getFocusedChild();
		if (focusedChild != null) {
			focusedChild.clearFocus();
		}
		final int move = newY - nowY;
		final int absMove = Math.abs(move);
		int duration = SCROLL_DURATION;
		if (absMove < cellLineLength) {
			duration = SCROLL_DURATION * absMove / cellLineLength;
		}
		// 启动恢复动画
		scroller.startScroll(0, nowY, 0, move, duration);
		invalidate();
	}
	
	private void scroll2Head(){
		final int nowY = getScrollY();
		final int newY = 0;
		enableChildrenCache(true);

		View focusedChild = getFocusedChild();
		if (focusedChild != null) {
			focusedChild.clearFocus();
		}
		final int move = newY - nowY;
		final int absMove = Math.abs(move);
		int duration = SCROLL_DURATION;
		if (absMove < cellLineLength) {
			duration = SCROLL_DURATION * absMove / cellLineLength;
		}
		// 启动恢复动画
		scroller.startScroll(0, nowY, 0, move, duration);
		invalidate();
	}
	
	public boolean onInterceptTouchEvent(MotionEvent event) {
		LogOut.out(this, "onInterceptTouchEvent action=" + event.getAction());
		final int action = event.getAction();
		if (action == MotionEvent.ACTION_MOVE && state != STATE_STATIC) {
			// MOVE及非静止情况下，返回TRUE阻止将此事件传递给子组件，
			// 而是执行onTouchEvent()来实现滑动
			return true;
		}
		final float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastMotionY = y;
			// 点击按钮时，此处设置状态为静止。
			state = scroller.isFinished() ? STATE_STATIC : STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			if (state == STATE_STATIC) {
				// 由于已静止，在点击按钮后进行拖拉，则根据拖拉位移大小决定是否需要改变状态进而进一步拦截此事件。
				checkScrolling(y);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			enableChildrenCache(false);
			state = STATE_STATIC;
			break;
		}
		// 非静止状态，将此事件交由onTouchEvent()处理。
		return state != STATE_STATIC;
	}
	
	/** 与Scroller相匹配，实现动画效果中每一帧的界面更新。 */
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		}
	}
	
	private void checkScrolling(float y) {
		float diff = Math.abs(y - lastMotionY);
		if (diff > distanceSlop) {
			state = STATE_SCROLLING;
			enableChildrenCache(true);
		}
	}
	/**
	 * 开始滑动时设置允许使用缓存。<br/>
	 * 结束滑动时设置取消缓存。<br/>
	 */
	public void enableChildrenCache(boolean enable) {
		setChildrenDrawingCacheEnabled(enable);
		setChildrenDrawnWithCacheEnabled(enable);
	}

	/** 让拖拉、动画过程中界面过渡顺滑。 */
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
//		final long drawingTime = getDrawingTime();
//
//		final int count = getChildCount();
//		LogOut.out(this, "dispatch count="+count);
//		for (int i = 0; i < count; i++) {
//			drawChild(canvas, getChildAt(i), drawingTime);
//		}
		
	}
}