package Controllers;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.example.android.carparkappv1.R;
import com.example.android.carparkappv1.Shared;

import java.util.ArrayList;
import java.util.List;

import Fragments.MenuFragment;
import Fragments.MyCustomMap;
import Fragments.SaveLotNumber;
import Fragments.TestFrag;


/**
 * Acts as facade to load different screens.
 */
public class ScreenController {
    public static ScreenController mInstance = null;
    private static List<Screen> openedScreens = new ArrayList<Screen>();
    private FragmentManager mFragmentManager;



    public static ScreenController getInstance() {
        if (mInstance == null) {
            mInstance = new ScreenController();
        }
        return mInstance;
    }

    public static Screen getLastScreen() {
        return openedScreens.get(openedScreens.size() - 1);
    }

    public boolean onBack() {
        if (openedScreens.size() > 0) {
            Screen screenToRemove = openedScreens.get(openedScreens.size() - 1);
            openedScreens.remove(openedScreens.size() - 1);
            if (openedScreens.size() == 0) {
                return true;
            }
            Screen screen = openedScreens.get(openedScreens.size() - 1);
            openedScreens.remove(openedScreens.size() - 1);
            openScreen(screen);

            return false;
        }
        return true;
    }


    public enum Screen {
        MENU,
        MFH,
        TEST,
        SAVELOT,

    }

    public void openScreen(Screen screen){
        mFragmentManager = Shared.activity.getFragmentManager();
        Fragment fragment = getFragment(screen);
        if(fragment!= null){
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment, "CURRENT_FRAG");
            fragmentTransaction.commit();
            mFragmentManager.executePendingTransactions();
        }
        openedScreens.add(screen);

    }
    //overloaded method to pass in data for mapfragment
    public void openScreen(Screen screen, String destination){
        mFragmentManager = Shared.activity.getFragmentManager();
        MyCustomMap fragment = (MyCustomMap) getFragment(screen);
        fragment.setDestination(destination);
        if(fragment!= null){
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment, "CURRENT_FRAG");
            fragmentTransaction.commit();
            mFragmentManager.executePendingTransactions();
        }
        openedScreens.add(screen);

    }

    public Fragment getFragment(Screen screen){
        Fragment frag = null;
        switch(screen){
            case MENU:
                frag = new MenuFragment();
                break;
            case MFH:
                frag = new MyCustomMap();
                break;
            case TEST:
                frag = new TestFrag();
                break;
            case SAVELOT:
                frag = new SaveLotNumber();
        }
        return frag;
    }
}
