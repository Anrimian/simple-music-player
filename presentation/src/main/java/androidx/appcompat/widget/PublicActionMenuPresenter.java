package androidx.appcompat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.appcompat.view.menu.MenuItemImpl;

import com.github.anrimian.musicplayer.ui.utils.views.ViewClickUtil;

import java.util.ArrayList;

public class PublicActionMenuPresenter extends ActionMenuPresenter {

    private OverflowMenuRunnable overflowMenuAction;

    public PublicActionMenuPresenter(Context context) {
        super(context);
    }

    public PublicActionMenuPresenter(Context context, OverflowMenuRunnable overflowMenuAction) {
        super(context);
        this.overflowMenuAction = overflowMenuAction;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean showOverflowMenu() {
        if (overflowMenuAction == null) {
            return super.showOverflowMenu();
        } else {
            //we can't send usual 'callback' to super class so just filter often calls
            ViewClickUtil.filterFastClick(
                    () -> overflowMenuAction.call(mOverflowButton, mMenu.getNonActionItems()),
                    500);
            return true;
        }
    }

    public interface OverflowMenuRunnable {
        void call(View anchorView, ArrayList<MenuItemImpl> items);
    }
}
