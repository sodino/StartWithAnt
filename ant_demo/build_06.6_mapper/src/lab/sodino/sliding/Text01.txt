package lab.sodino.sliding;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @author Sodino E-mail:sodinoopen@hotmail.com
 * @version Time：2012-1-18 下午02:55:59
 */
public class SlidingContainer extends ViewGroup {
	private static final int INVALID_SCREEN = -1;
	public static final int SCROLL_DURATION = 500;
	public static final int SPEC_UNDEFINED = ViewGroup.LayoutParams.FILL_PARENT;
	public static final int SNAP_VELOCITY = 500;
	private static final int STATE_STATIC = 0;
	private static final int STATE_SCROLLING = 1;
	private int pageWidth;
	/**
	 * 标识是否是第一次布局。<br/>
	 * 第一次布局需要将第一页调居中显示在屏幕上。<br/>
	 */
	private boolean isFirstLayout;
	private int currentPage, nextPage;
	private Scroller scroller;
	/** 手指滑动过程中可理解为拖动的最小长度。 */
	private int distanceSlop;
	private int state = STATE_STATIC;
	private float lastMotionX;
	private OnSlidingListener slidingListener;

	public SlidingContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LogOut.out(this, "SlidingContainer() 3");
		initialization(context, attrs);
	}

	public SlidingContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		LogOut.out(this, "SlidingContainer() 2");
		initialization(context, attrs);
	}

	public SlidingContainer(Context context) {
		super(context);
		LogOut.out(this, "SlidingContainer() 1");
		initialization(context, null);
	}

	private void initialization(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray typedArr = context.obtainStyledAttributes(attrs, R.styleable.sliding_SlidingContainer);
			pageWidth = typedArr.getDimensionPixelSize(R.styleable.sliding_SlidingContainer_pageWidth, SPEC_UNDEFINED);
			typedArr.recycle();
		}

		state = STATE_STATIC;
		isFirstLayout = true;
		currentPage = 0;
		nextPage = INVALID_SCREEN;

		scroller = new Scroller(context);

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		distanceSlop = configuration.getScaledTouchSlop();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getScrollXByPage(int page) {
		return (page * pageWidth) - getPagePadding();
	}

	public int getPagePadding() {
		return (getMeasuredWidth() - pageWidth) >> 1;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public boolean scroll2page(int page) {
		if (page < 0) {
			return false;
		} else if (page >= getChildCount()) {
			return false;
		} else if (scroller.isFinished() == false) {
			return false;
		}
		enableChildrenCache(true);
		boolean changingPage = (page != currentPage);
		nextPage = page;

		View focusedChild = getFocusedChild();
		if (changingPage && focusedChild != null && focusedChild == getChildAt(currentPage)) {
			focusedChild.clearFocus();
		}

		final int nowX = getScrollX();
		final int newX = getScrollXByPage(nextPage);
		final int move = newX - nowX;
		final int absMove = Math.abs(move);
		int duration = SCROLL_DURATION;
		if (absMove < pageWidth) {
			duration = SCROLL_DURATION * absMove / pageWidth;
		}
		// 启动左右切屏动画
		scroller.startScroll(nowX, 0, move, 0, duration);
		invalidate();
		return true;
	}

	private void checkScrolling(float x) {
		float diff = Math.abs(x - lastMotionX);
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

	/** 在正式显示之前设置才有效。 */
	public boolean setPageWidth(int width) {
		if (isFirstLayout) {
			pageWidth = width;
			return true;
		}
		return false;
	}

	public void setOnSlidingListener(OnSlidingListener listener) {
		slidingListener = listener;
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogOut.out(this, "onMeasure()");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		pageWidth = (pageWidth == SPEC_UNDEFINED) ? getMeasuredWidth() : pageWidth;
		pageWidth = Math.min(Math.max(0, pageWidth), getMeasuredWidth());

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			int childWidthSpec = MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY);
			View view = getChildAt(i);
			view.measure(childWidthSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changing, int left, int top, int right, int bottom) {
		LogOut.out(this, "onLayout");
		int childLeft = 0;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View view = getChildAt(i);
			if (view.getVisibility() != View.GONE) {
				int childWidth = view.getMeasuredWidth();
				view.layout(childLeft, 0, childLeft + childWidth, view.getMeasuredHeight());
				childLeft += childWidth;
			}
		}

		if (isFirstLayout) {
			scrollTo(getScrollXByPage(currentPage), 0);
			isFirstLayout = false;
		}
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		LogOut.out(this, "onInterceptTouchEvent action=" + event.getAction());
		final int action = event.getAction();
		if (action == MotionEvent.ACTION_MOVE && state != STATE_STATIC) {
			// MOVE及非静止情况下，返回TRUE阻止将此事件传递给子组件，
			// 而是执行onTouchEvent()来实现滑动
			return true;
		}
		final float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastMotionX = x;
			// 点击按钮时，此处设置状态为静止。
			state = scroller.isFinished() ? STATE_STATIC : STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			if (state == STATE_STATIC) {
				// 由于已静止，在点击按钮后进行拖拉，则根据拖拉位移大小决定是否需要改变状态进而进一步拦截此事件。
				checkScrolling(x);
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

	public boolean onTouchEvent(MotionEvent event) {
		LogOut.out(this, "onTouchEvent");
		super.onTouchEvent(event);
		final int action = event.getAction();
		final float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastMotionX = x;
			if (scroller.isFinished() == false) {
				scroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (state == STATE_STATIC) {
				checkScrolling(x);
			} else if (state == STATE_SCROLLING) {
				int moveX = (int) (lastMotionX - x);
				lastMotionX = x;
				if (getScrollX() < 0 || getScrollX() > getChildAt(getChildCount() - 1).getLeft()) {
					// 对于越界的拖拉，则将位移减半。
					moveX = moveX >> 1;
				}
				scrollBy(moveX, 0);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (state == STATE_SCROLLING) {
				final int startX = getScrollXByPage(currentPage);
				// 默认选择回到手指滑动之前的当前页
				int whichPage = currentPage;
				int xSpace = getWidth() / 8;
				if (getScrollX() < startX - xSpace) {
					whichPage = Math.max(0, whichPage - 1);
				} else if (getScrollX() > startX + xSpace) {
					whichPage = Math.min(getChildCount() - 1, whichPage + 1);
				}
				scroll2page(whichPage);
			}
			state = STATE_STATIC;
			break;
		}
		return true;
	}

	/** 让拖拉、动画过程中界面过渡顺滑。 */
	protected void dispatchDraw(Canvas canvas) {
		final long drawingTime = getDrawingTime();

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			drawChild(canvas, getChildAt(i), drawingTime);
		}

		if (slidingListener != null) {
			int adjustedScrollX = getScrollX() + getPagePadding();
			slidingListener.onSliding(adjustedScrollX);
			if (adjustedScrollX % pageWidth == 0) {
				slidingListener.onSlidingEnd(adjustedScrollX / pageWidth, adjustedScrollX);
			}
		}
	}

	/** 与Scroller相匹配，实现动画效果中每一帧的界面更新。 */
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		} else if (nextPage != INVALID_SCREEN) {
			currentPage = nextPage;
			nextPage = INVALID_SCREEN;
			enableChildrenCache(false);
		}
	}

	/** 无法确定何时被调用。 */
	public boolean dispatchUnhandledMove(View view, int direction) {
		LogOut.out(this, "dispatchUnhandledMove view:" + view);
		if (direction == View.FOCUS_LEFT) {
			if (currentPage > 0) {
				scroll2page(currentPage - 1);
				return true;
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (currentPage < getChildCount() - 1) {
				scroll2page(currentPage + 1);
				return true;
			}
		}
		return super.dispatchUnhandledMove(view, direction);
	}

	/** 无法确定何时被调用。 */
	public void addFocusables(ArrayList<View> views, int direction) {
		LogOut.out(this, "addFocusables");
		getChildAt(currentPage).addFocusables(views, direction);
		if (direction == View.FOCUS_LEFT) {
			if (currentPage > 0) {
				getChildAt(currentPage - 1).addFocusables(views, direction);
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (currentPage < getChildCount() - 1) {
				getChildAt(currentPage + 1).addFocusables(views, direction);
			}
		}

	}

	/** 无法确定何时被调用。 */
	protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
		LogOut.out(this, "onRequestFocusInDescendants direction=" + direction + " rect:" + previouslyFocusedRect);
		int focusableScreen = currentPage;
		if (nextPage != INVALID_SCREEN) {
			focusableScreen = nextPage;
		}
		getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
		return false;
	}

	public static interface OnSlidingListener {
		public void onSliding(int scrollX);

		public void onSlidingEnd(int pageIdx, int scrollX);
	}
}