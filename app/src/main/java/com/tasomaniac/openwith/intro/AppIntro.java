package com.tasomaniac.openwith.intro;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tasomaniac.openwith.R;

import java.util.List;
import java.util.Vector;

public abstract class AppIntro extends AppCompatActivity {
    public final static int DEFAULT_COLOR = 1;

    protected PagerAdapter mPagerAdapter;
    protected AppIntroViewPager pager;
    protected List<Fragment> fragments = new Vector<>();
    protected List<ImageView> dots;
    protected int slidesNumber;
    protected Vibrator mVibrator;
    protected CircularIndicatorView mController;
    protected boolean isVibrateOn = false;
    protected int vibrateIntensity = 20;
    protected boolean skipButtonEnabled = true;
    protected boolean baseProgressButtonEnabled = true;
    protected boolean progressButtonEnabled = true;
    protected int selectedIndicatorColor = DEFAULT_COLOR;
    protected int unselectedIndicatorColor = DEFAULT_COLOR;
    protected View skipButton;
    protected View nextButton;
    protected View doneButton;
    protected int savedCurrentItem;

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.intro_layout);

        skipButton = findViewById(R.id.skip);
        nextButton = findViewById(R.id.next);
        doneButton = findViewById(R.id.done);
        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        pager = (AppIntroViewPager) findViewById(R.id.view_pager);
        pager.setAdapter(this.mPagerAdapter);

        if (savedInstanceState != null) {
            restoreLockingState(savedInstanceState);
        }

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                if (isVibrateOn) {
                    mVibrator.vibrate(vibrateIntensity);
                }
                onSkipPressed();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                if (isVibrateOn) {
                    mVibrator.vibrate(vibrateIntensity);
                }
                pager.setCurrentItem(pager.getCurrentItem() + 1);
                onNextPressed();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                if (isVibrateOn) {
                    mVibrator.vibrate(vibrateIntensity);
                }
                onDonePressed();
            }
        });

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        pager = (AppIntroViewPager) findViewById(R.id.view_pager);

        pager.setAdapter(this.mPagerAdapter);

        /**
         *  ViewPager.setOnPageChangeListener is now deprecated. Use addOnPageChangeListener() instead of it.
         */
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (slidesNumber > 1)
                    mController.selectPosition(position);

                // Allow the swipe to be re-enabled if a user swipes to a previous slide. Restore
                // state of progress button depending on global progress button setting
                if (!pager.isNextPagingEnabled()) {
                    if (pager.getCurrentItem() != pager.getLockPage()) {
                        setProgressButtonEnabled(baseProgressButtonEnabled);
                        pager.setNextPagingEnabled(true);
                    } else {
                        setProgressButtonEnabled(progressButtonEnabled);
                    }
                } else {
                    setProgressButtonEnabled(progressButtonEnabled);
                }
                setButtonState(skipButton, skipButtonEnabled);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setCurrentItem(savedCurrentItem); //required for triggering onPageSelected for first page

        init(savedInstanceState);
        slidesNumber = fragments.size();

        if (slidesNumber == 1) {
            setProgressButtonEnabled(progressButtonEnabled);
        } else {
            initController();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("baseProgressButtonEnabled", baseProgressButtonEnabled);
        outState.putBoolean("progressButtonEnabled", progressButtonEnabled);
        outState.putBoolean("skipButtonEnabled", skipButtonEnabled);
        outState.putBoolean("nextEnabled", pager.isPagingEnabled());
        outState.putBoolean("nextPagingEnabled", pager.isNextPagingEnabled());
        outState.putInt("lockPage", pager.getLockPage());
        outState.putInt("currentItem", pager.getCurrentItem());
    }


    protected void restoreLockingState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.baseProgressButtonEnabled = savedInstanceState.getBoolean("baseProgressButtonEnabled");
        this.progressButtonEnabled = savedInstanceState.getBoolean("progressButtonEnabled");
        this.skipButtonEnabled = savedInstanceState.getBoolean("skipButtonEnabled");
        this.savedCurrentItem = savedInstanceState.getInt("currentItem");
        pager.setPagingEnabled(savedInstanceState.getBoolean("nextEnabled"));
        pager.setNextPagingEnabled(savedInstanceState.getBoolean("nextPagingEnabled"));
        pager.setLockPage(savedInstanceState.getInt("lockPage"));
    }

    public AppIntroViewPager getPager() {
        return pager;
    }

    private void initController() {
        mController = (CircularIndicatorView) findViewById(R.id.indicator);

        mController.initialize(slidesNumber);
        if (selectedIndicatorColor != DEFAULT_COLOR)
            mController.setSelectedIndicatorColor(selectedIndicatorColor);
        if (unselectedIndicatorColor != DEFAULT_COLOR)
            mController.setUnselectedIndicatorColor(unselectedIndicatorColor);
    }

    public void addSlide(@NonNull Fragment fragment) {
        fragments.add(fragment);
        mPagerAdapter.notifyDataSetChanged();
    }

    @NonNull
    public List<Fragment> getSlides() {
        return mPagerAdapter.getFragments();
    }

    public boolean isProgressButtonEnabled() {
        return progressButtonEnabled;
    }

    public boolean isSkipButtonEnabled() {
        return skipButtonEnabled;
    }

    private void setButtonState(View button, boolean show) {
        if (show) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.INVISIBLE);
        }
    }

    public void setOffScreenPageLimit(int limit) {
        pager.setOffscreenPageLimit(limit);
    }

    public abstract void init(@Nullable Bundle savedInstanceState);

    public abstract void onSkipPressed();

    public abstract void onNextPressed();

    public abstract void onDonePressed();

    @Override
    public boolean onKeyDown(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_BUTTON_A || code == KeyEvent.KEYCODE_DPAD_CENTER) {
            ViewPager vp = (ViewPager) this.findViewById(R.id.view_pager);
            if (vp.getCurrentItem() == vp.getAdapter().getCount() - 1) {
                onDonePressed();
            } else {
                vp.setCurrentItem(vp.getCurrentItem() + 1);
            }
            return false;
        }
        return super.onKeyDown(code, event);
    }

    /**
     * Setting to to display or hide the Next or Done button. This is a static setting and
     * button state is maintained across slides until explicitly changed.
     *
     * @param progressButtonEnabled Set true to display. False to hide.
     */
    public void setProgressButtonEnabled(boolean progressButtonEnabled) {
        this.progressButtonEnabled = progressButtonEnabled;
        if (progressButtonEnabled) {
            if (pager.getCurrentItem() == slidesNumber - 1) {
                setButtonState(nextButton, false);
                setButtonState(doneButton, true);
            } else {
                setButtonState(nextButton, true);
                setButtonState(doneButton, false);
            }
        } else {
            setButtonState(nextButton, false);
            setButtonState(doneButton, false);
        }
    }

    /**
     * Override viewpager bar color
     *
     * @param color your color resource
     */
    public void setBarColor(@ColorInt final int color) {
        LinearLayout bottomBar = (LinearLayout) findViewById(R.id.bottom);
        bottomBar.setBackgroundColor(color);
    }

    /**
     * Override skip text
     *
     * @param text your text
     */
    public void setSkipText(@Nullable final String text) {
        TextView skipText = (TextView) findViewById(R.id.skip);
        skipText.setText(text);
    }

    /**
     * Override done text
     *
     * @param text your text
     */
    public void setDoneText(@Nullable final String text) {
        TextView doneText = (TextView) findViewById(R.id.done);
        doneText.setText(text);
    }

    /**
     * Override done button text color
     *
     * @param colorDoneText your color resource
     */
    public void setColorDoneText(@ColorInt final int colorDoneText) {
        TextView doneText = (TextView) findViewById(R.id.done);
        doneText.setTextColor(colorDoneText);
    }

    /**
     * Override skip button color
     *
     * @param colorSkipButton your color resource
     */
    public void setColorSkipButton(@ColorInt final int colorSkipButton) {
        TextView skip = (TextView) findViewById(R.id.skip);
        skip.setTextColor(colorSkipButton);
    }

    /**
     * Override Next button
     *
     * @param imageNextButton your drawable resource
     */
    public void setImageNextButton(@DrawableRes final Drawable imageNextButton) {
        final ImageView nextButton = (ImageView) findViewById(R.id.next);
        nextButton.setImageDrawable(imageNextButton);

    }

    /**
     * Allows the user to set the nav bar color of their app intro
     *
     * @param Color string form of color in 3 or 6 digit hex form (#ffffff)
     */
    public void setNavBarColor(String Color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(android.graphics.Color.parseColor(Color));
        }
    }

    /**
     * Allows the user to set the nav bar color of their app intro
     *
     * @param color int form of color. pass your color resource to here (R.color.your_color)
     */
    public void setNavBarColor(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, color));
        }
    }

    /**
     * Allows for setting statusbar visibility (true by default)
     *
     * @param isVisible put true to show status bar, and false to hide it
     */
    public void showStatusBar(boolean isVisible) {
        if (!isVisible) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Setting to to display or hide the Skip button. This is a static setting and
     * button state is maintained across slides until explicitly changed.
     *
     * @param showButton Set true to display. False to hide.
     */
    public void showSkipButton(boolean showButton) {
        this.skipButtonEnabled = showButton;
        setButtonState(skipButton, showButton);
    }

    /**
     * Shows or hides Done button, replaced with setProgressButtonEnabled
     *
     * @deprecated use {@link #setProgressButtonEnabled(boolean)} instead.
     */
    @Deprecated
    public void showDoneButton(boolean showDone) {
        setProgressButtonEnabled(showDone);
    }

    /**
     * sets vibration when buttons are pressed
     *
     * @param vibrationEnabled on/off
     */
    public void setVibrate(boolean vibrationEnabled) {
        this.isVibrateOn = vibrationEnabled;
    }

    /**
     * sets vibration intensity
     *
     * @param intensity desired intensity
     */
    public void setVibrateIntensity(int intensity) {
        this.vibrateIntensity = intensity;
    }

    /**
     * Set a custom {@link CircularIndicatorView} to use a custom indicator view for the {@link AppIntro} instead of the
     * default one.
     *
     * @param controller The controller to use
     */
    public void setCustomIndicator(@NonNull CircularIndicatorView controller) {
        mController = controller;
    }

    /**
     * Overrides color of selected and unselected indicator colors
     * <p/>
     * Set DEFAULT_COLOR for color value if you don't want to change it
     *
     * @param selectedIndicatorColor   your selected color
     * @param unselectedIndicatorColor your unselected color
     */
    public void setIndicatorColor(int selectedIndicatorColor, int unselectedIndicatorColor) {
        this.selectedIndicatorColor = selectedIndicatorColor;
        this.unselectedIndicatorColor = unselectedIndicatorColor;

        if (mController != null) {
            if (selectedIndicatorColor != DEFAULT_COLOR)
                mController.setSelectedIndicatorColor(selectedIndicatorColor);
            if (unselectedIndicatorColor != DEFAULT_COLOR)
                mController.setUnselectedIndicatorColor(unselectedIndicatorColor);
        }
    }

    /**
     * Setting to disable forward swiping right on current page and allow swiping left. If a swipe
     * left occurs, the lock state is reset and swiping is re-enabled. (one shot disable) This also
     * hides/shows the Next and Done buttons accordingly.
     *
     * @param lockEnable Set true to disable forward swiping. False to enable.
     */
    public void setNextPageSwipeLock(boolean lockEnable) {
        if (lockEnable) {
            // if locking, save current progress button visibility
            baseProgressButtonEnabled = progressButtonEnabled;
            setProgressButtonEnabled(!lockEnable);
        } else {
            // if unlocking, restore original button visibility
            setProgressButtonEnabled(baseProgressButtonEnabled);
        }
        pager.setNextPagingEnabled(!lockEnable);
    }

    /**
     * Setting to disable swiping left and right on current page. This also
     * hides/shows the Next and Done buttons accordingly.
     *
     * @param lockEnable Set true to disable forward swiping. False to enable.
     */
    public void setSwipeLock(boolean lockEnable) {
        if (lockEnable) {
            // if locking, save current progress button visibility
            baseProgressButtonEnabled = progressButtonEnabled;
            //setProgressButtonEnabled(!lockEnable);
        } else {
            // if unlocking, restore original button visibility
            setProgressButtonEnabled(baseProgressButtonEnabled);
        }
        pager.setPagingEnabled(!lockEnable);
    }

    static class PagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragments;

        public PagerAdapter(FragmentManager fm, @NonNull List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }

        @NonNull
        public List<Fragment> getFragments() {
            return fragments;
        }
    }
}
