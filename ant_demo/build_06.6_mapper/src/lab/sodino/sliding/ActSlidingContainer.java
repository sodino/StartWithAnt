package lab.sodino.sliding;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ActSlidingContainer extends Activity implements OnClickListener {
	private CellLayout cellLayout;
	private View viewAdded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_cell_layout);
		cellLayout = (CellLayout) findViewById(R.id.cellLayout);
		initViews();
	}

	private void initViews() {
		viewAdded = (View) findViewById(R.id.viewAdded);
		viewAdded.setOnClickListener(this);

		for (int i = 0; i < 2; i++) {
			addView2CellLayout();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.viewAdded:
			addView2CellLayout();
			LogOut.out(this, "click addView");
			break;
		default:
			// 不设置，在删除时会有奇怪的现象
			v.setVisibility(View.GONE);
			cellLayout.removeView(v);
			break;
		}
	}

	private void addView2CellLayout() {
		int idx = cellLayout.getChildCount() - 1;
		TextView txtView = new TextView(this);
		txtView.setText("Item:" + idx);
		txtView.setTextSize(30);
		txtView.setGravity(Gravity.CENTER);
		txtView.setTextColor(Color.BLACK);
		txtView.setBackgroundColor(Color.CYAN);
		txtView.setOnClickListener(this);
		cellLayout.addView(txtView, idx);
	}
}