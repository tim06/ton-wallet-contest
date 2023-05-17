package com.github.tim06.wallet_contest.ui.components.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.Image;
import android.os.Build;
import android.text.Editable;
import android.text.TextPaint;
import android.util.StateSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.core.graphics.ColorUtils;
import androidx.core.view.GestureDetectorCompat;

import com.github.tim06.wallet_contest.R;

public class CustomPhoneKeyboardView extends ViewGroup {
    public final static int KEYBOARD_HEIGHT_DP = 230;

    private final static int SIDE_PADDING = 10, BUTTON_PADDING = 6;

    private float density = 0;
    private ImageView backButton;
    private EditText editText;
    private View[] views = new View[12];

    private OnKeyboardQueryListener listener;

    private boolean isDarkMode;
    private boolean isInputEnabled;
    private boolean dispatchBackWhenEmpty;
    private boolean runningLongClick;
    private Runnable onBackButton = () -> {
        //if (editText == null || editText.length() == 0 && !dispatchBackWhenEmpty) return;

        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        playSoundEffect(SoundEffectConstants.CLICK);
        if (isInputEnabled) {
            listener.onDeleteClick();
        }
        //editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        //editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));

        if (runningLongClick) {
            postDelayed(this.onBackButton, 50);
        }
    };

    private boolean postedLongClick;
    private Runnable detectLongClick = () -> {
        postedLongClick = false;
        runningLongClick = true;
        onBackButton.run();
    };

    public CustomPhoneKeyboardView(Context context, OnKeyboardQueryListener listener, boolean isDarkMode, boolean withDot, boolean withBiometric) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        this.listener = listener;
        this.isDarkMode = isDarkMode;

        for (int i = 0; i < 11; i++) {
            if (i == 9 && !withDot) continue;

            String symbols;
            switch (i) {
                default:
                case 0:
                    symbols = "";
                    break;
                case 1:
                    symbols = "ABC";
                    break;
                case 2:
                    symbols = "DEF";
                    break;
                case 3:
                    symbols = "GHI";
                    break;
                case 4:
                    symbols = "JKL";
                    break;
                case 5:
                    symbols = "MNO";
                    break;
                case 6:
                    symbols = "PQRS";
                    break;
                case 7:
                    symbols = "TUV";
                    break;
                case 8:
                    symbols = "WXYZ";
                    break;
                case 10:
                    symbols = "+";
                    break;
            }
            String num;
            if (i == 9) {
                num = ".";
            } else if (i == 10) {
                num = String.valueOf(0);
            } else {
                num = String.valueOf(i + 1);
            }
            views[i] = new NumberButtonView(context, num, symbols);
            views[i].setOnClickListener(v -> {
                if (isInputEnabled) {
                    listener.onClick(num);
                }
                if (editText == null) return;

                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

                Editable text = editText.getText();
                int newSelection = editText.getSelectionEnd() == editText.length() ? -1 : editText.getSelectionStart() + num.length();
                if (editText.getSelectionStart() != -1 && editText.getSelectionEnd() != -1) {
                    editText.setText(text.replace(editText.getSelectionStart(), editText.getSelectionEnd(), num));
                    editText.setSelection(newSelection == -1 ? editText.length() : newSelection);
                } else {
                    editText.setText(num);
                    editText.setSelection(editText.length());
                }
            });
            addView(views[i]);
        }

        GestureDetectorCompat backDetector = setupBackButtonDetector(context);
        backButton = new ImageView(context) {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (postedLongClick || runningLongClick) {
                        postedLongClick = false;
                        runningLongClick = false;
                        removeCallbacks(detectLongClick);
                        removeCallbacks(onBackButton);
                    }
                }
                super.onTouchEvent(event);
                return backDetector.onTouchEvent(event);
            }
        };
        backButton.setImageResource(R.drawable.msg_clear_input);
        if (isDarkMode) {
            backButton.setColorFilter(Color.WHITE);
        } else {
            backButton.setColorFilter(Color.BLACK);
        }
        backButton.setBackground(getButtonDrawable());
        int pad = dp(density,11);
        backButton.setPadding(pad, pad, pad, pad);
        backButton.setOnClickListener(v -> {});
        addView(views[11] = backButton);

        if (withBiometric) {
            ImageView biometricImageView = new ImageView(context);
            biometricImageView.setImageResource(R.drawable.ic_fingerprint);
            if (isDarkMode) {
                biometricImageView.setColorFilter(Color.WHITE);
            } else {
                biometricImageView.setColorFilter(Color.BLACK);
            }
            biometricImageView.setBackground(getButtonDrawable());
            biometricImageView.setPadding(pad, pad, pad, pad);
            biometricImageView.setOnClickListener(v -> {
                listener.onBiometricClick();
            });
            addView(views[9] = biometricImageView);
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return true;
    }

    public void setDispatchBackWhenEmpty(boolean dispatchBackWhenEmpty) {
        this.dispatchBackWhenEmpty = dispatchBackWhenEmpty;
    }

    private GestureDetectorCompat setupBackButtonDetector(Context context) {
        int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        return new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                if (postedLongClick) {
                    removeCallbacks(detectLongClick);
                }
                postedLongClick = true;
                postDelayed(detectLongClick, 200);
                onBackButton.run();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if ((postedLongClick || runningLongClick) && (Math.abs(distanceX) >= touchSlop || Math.abs(distanceY) >= touchSlop)) {
                    postedLongClick = false;
                    runningLongClick = false;
                    removeCallbacks(detectLongClick);
                    removeCallbacks(onBackButton);
                }
                return false;
            }
        });
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
        dispatchBackWhenEmpty = false;
    }

    public void setInputEnabled(boolean enabled) {
        isInputEnabled = enabled;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int btnWidth = (getWidth() - dp(density, SIDE_PADDING * 2 + BUTTON_PADDING * 2)) / 3;
        int btnHeight = (getHeight() - dp(density, SIDE_PADDING * 3 + BUTTON_PADDING * 2)) / 4;

        for (int i = 0; i < views.length; i++) {
            int rowX = i % 3, rowY = i / 3;
            int left = rowX * (btnWidth + dp(density, BUTTON_PADDING)) + dp(density, SIDE_PADDING);
            int top = rowY * (btnHeight + dp(density, BUTTON_PADDING)) + dp(density, SIDE_PADDING);
            if (views[i] != null) {
                views[i].layout(left, top, left + btnWidth, top + btnHeight);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

        int btnWidth = (getWidth() - dp(density, SIDE_PADDING * 2 + BUTTON_PADDING * 2)) / 3;
        int btnHeight = (getHeight() - dp(density, SIDE_PADDING * 3 + BUTTON_PADDING * 2)) / 4;

        for (View v : views) {
            if (v != null) {
                v.measure(MeasureSpec.makeMeasureSpec(btnWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(btnHeight, MeasureSpec.EXACTLY));
            }
        }
    }

    private Drawable getButtonDrawable() {
        int color;
        if (isDarkMode) {
            color = Color.parseColor("#1FFFFFFF");
        } else {
            color = Color.parseColor("#F0F0F0");
        }
        return createSimpleSelectorRoundRectDrawable(dp(density, 6), color, ColorUtils.setAlphaComponent(color, 60));
    }

    public void updateColors() {
        if (isDarkMode) {
            backButton.setColorFilter(Color.WHITE);
        } else {
            backButton.setColorFilter(Color.BLACK);
        }
        for (View v : views) {
            if (v != null) {
                v.setBackground(getButtonDrawable());

                if (v instanceof NumberButtonView) {
                    ((NumberButtonView) v).updateColors();
                }
            }
        }
    }

    private final class NumberButtonView extends View {
        private TextPaint numberTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private TextPaint symbolsTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private String mNumber, mSymbols;
        private Rect rect = new Rect();

        public NumberButtonView(Context context, String number, String symbols) {
            super(context);
            mNumber = number;
            mSymbols = symbols;

            numberTextPaint.setTextSize(dp(density, 24));
            symbolsTextPaint.setTextSize(dp(density, 14));

            setBackground(getButtonDrawable());
            updateColors();
        }

        private void updateColors() {
            if (isDarkMode) {
                numberTextPaint.setColor(Color.WHITE);
            } else {
                numberTextPaint.setColor(Color.BLACK);
            }
            symbolsTextPaint.setColor(Color.parseColor("#ffa8a8a8"));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float symbolsWidth = symbolsTextPaint.measureText(mSymbols);
            float numberWidth = numberTextPaint.measureText(mNumber);

            numberTextPaint.getTextBounds(mNumber, 0, mNumber.length(), rect);
            float textOffsetNumber = rect.height() / 2f;
            symbolsTextPaint.getTextBounds(mSymbols, 0, mSymbols.length(), rect);
            float textOffsetSymbols = rect.height() / 2f;

            canvas.drawText(mNumber, getWidth() * 0.25f - numberWidth / 2f, getHeight() / 2f + textOffsetNumber, numberTextPaint);
            canvas.drawText(mSymbols, getWidth() * 0.7f - symbolsWidth / 2f, getHeight() / 2f + textOffsetSymbols, symbolsTextPaint);
        }
    }

    public interface OnKeyboardQueryListener {
        void onClick(String number);
        void onDeleteClick();
        void onBiometricClick();
    }

    public static int dp(float density, float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor) {
        return createSimpleSelectorRoundRectDrawable(rad, defaultColor, pressedColor, pressedColor);
    }

    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor, int maskColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        pressedDrawable.getPaint().setColor(maskColor);
        if (Build.VERSION.SDK_INT >= 21) {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{pressedColor}
            );
            return new RippleDrawable(colorStateList, defaultDrawable, pressedDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
            stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
            return stateListDrawable;
        }
    }
}
