package lab.sodino.sliding;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

/**
 * @author Sodino E-mail:sodinoopen@hotmail.com
 * @version Time：2012-1-18 下午03:31:08
 */
public class SlidingIndicator extends View {
	public static final int BAR_COLOR = 0xaa777777;
	public static final int HIGHLIGHT_COLOR = 0xaa999999;
	public static final int FADE_DELAY = 2000;
	public static final int FADE_DURATION = 500;

	private int amount, currentPage, position;
	private Paint barPaint, highlightPaint;
	private int fadeDelay, fadeDuration;
	private float ovalRadius;
	private Animation animFadeout;
	/** RectF比Rect是精度上更精确。 */
	private RectF rectFBody, rectFIndicator;

	public SlidingIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 预设值。
		int barColor = BAR_COLOR, highlightColor = HIGHLIGHT_COLOR;
		fadeDelay = FADE_DELAY;
		fadeDuration = FADE_DURATION;
		if (attrs != null) {
			TypedArray typedArr = context.obtainStyledAttributes(attrs, R.styleable.sliding_SlidingIndicator);
			barColor = typedArr.getColor(R.styleable.sliding_SlidingIndicator_barColor, BAR_COLOR);
			highlightColor = typedArr.getColor(R.styleable.sliding_SlidingIndicator_highlightColor, HIGHLIGHT_COLOR);
			fadeDelay = typedArr.getInteger(R.styleable.sliding_SlidingIndicator_fadeDelay, FADE_DELAY);
			fadeDuration = typedArr.getInteger(R.styleable.sliding_SlidingIndicator_fadeDuration, FADE_DURATION);
			ovalRadius = typedArr.getDimension(R.styleable.sliding_SlidingIndicator_roundRectRadius, 0f);
			typedArr.recycle();
		}
		initialization(barColor, highlightColor, fadeDuration);
	}

	public SlidingIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingIndicator(Context context) {
		super(context);
	}

	private void initialization(int barColor, int highlightColor, int fadeDuration) {
		barPaint = new Paint();
		barPaint.setColor(barColor);

		highlightPaint = new Paint();
		highlightPaint.setColor(highlightColor);

		animFadeout = new AlphaAnimation(1f, 0f);
		animFadeout.setDuration(fadeDuration);
		animFadeout.setRepeatCount(0);
		animFadeout.setInterpolator(new LinearInterpolator());
		// 设置动画结束后，本组件保持动画结束时的最后状态，即全透明不可见。
		animFadeout.setFillEnabled(true);
		animFadeout.setFillAfter(true);

		rectFBody = new RectF();
		rectFIndicator = new RectF();
	}

	public void setPageAmount(int num) {
		if (num < 0) {
			throw new IllegalArgumentException("num must be positive.");
		}
		amount = num;
		invalidate();
		fadeOut();
	}

	private void fadeOut() {
		if (fadeDuration > 0) {
			clearAnimation();
			// 设置动画的延时时间，此时间段内正好指示当前页位置。
			animFadeout.setStartTime(AnimationUtils.currentAnimationTimeMillis() + fadeDelay);
			setAnimation(animFadeout);
		}
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int idx) {
		if (currentPage < 0 || currentPage >= amount) {
			throw new IllegalArgumentException("currentPage parameter out of bounds");
		}
		if (this.currentPage != idx) {
			this.currentPage = idx;
			this.position = currentPage * getPageWidth();
			invalidate();
			fadeOut();
		}
	}

	public void setPosition(int position) {
		if (this.position != position) {
			this.position = position;
			invalidate();
			fadeOut();
		}
	}

	public int getPageWidth() {
		return getWidth() / amount;
	}

	protected void onDraw(Canvas canvas) {
		rectFBody.set(0, 0, getWidth(), getHeight());
		canvas.drawRoundRect(rectFBody, ovalRadius, ovalRadius, barPaint);
		rectFIndicator.set(position, 0, position + getPageWidth(), getHeight());
		canvas.drawRoundRect(rectFIndicator, ovalRadius, ovalRadius, highlightPaint);
	}
}