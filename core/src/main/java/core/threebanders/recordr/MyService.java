package core.threebanders.recordr;/*package whatever //do not write package name here */
import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class MyService extends AccessibilityService {
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
	}

	@Override
	public void onInterrupt() {

	}
	@Override
	protected boolean onKeyEvent(KeyEvent event) {

		int action = event.getAction();
		int keyCode = event.getKeyCode();
		// the service listens for both pressing and releasing the key
		// so the below code executes twice, i.e. you would encounter two Toasts
		// in order to avoid this, we wrap the code inside an if statement
		// which executes only when the key is released
		if (action == KeyEvent.ACTION_UP) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				Log.d("Check", "KeyUp");
				Toast.makeText(this, "KeyUp", Toast.LENGTH_SHORT).show();
			} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				Log.d("Check", "KeyDown");
				Toast.makeText(this, "KeyDown", Toast.LENGTH_SHORT).show();
			}
		}
		return super.onKeyEvent(event);
	}
}
