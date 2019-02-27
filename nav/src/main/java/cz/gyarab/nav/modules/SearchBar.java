package cz.gyarab.nav.modules;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import cz.gyarab.nav.MainActivity;
import cz.gyarab.nav.R;

public class SearchBar extends ConstraintLayout {

    private boolean open;
    private SearchBarViewModel viewModel;
    private AutoCompleteTextView textView;
    private OptionSelectedListener listener;
    private ImageView background;
    private int width;

    public interface OptionSelectedListener{
        void onOptionSelected(String option);
    }

    public void setListener(OptionSelectedListener listener) {
        this.listener = listener;
    }

    public SearchBar(Context context) {
        super(context);
        init();
    }

    public SearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.search_bar, this);
        textView = findViewById(R.id.searchView);
        textView.setThreshold(1);

        //pozadi
        background = findViewById(R.id.search_background);
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setCornerRadius(100);
        backgroundDrawable.setColor(Color.WHITE);
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        background.setBackground(backgroundDrawable);

        //kliknutí na křížek
        findViewById(R.id.delete_search).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getText().toString().equals(""))
                    close();
                else
                    textView.setText("");
            }
        });

        //kliknutí na tlačítko done na klávesnici
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE) {
                    if (listener != null)
                        listener.onOptionSelected(v.getText().toString());
                    hideKeyboard();
                    clearFocus();
                }
                return true;
            }
        });

        //kliknutí na lupu
        findViewById(R.id.magni_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (open){
                    close();
                }
                else open();
            }
        });

        //po vybríní možnosti
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(parent.getItemAtPosition(position));
                hideKeyboard();
                textView.clearFocus();
                listener.onOptionSelected(parent.getItemAtPosition(position).toString());
                close();
            }
        });

        //skryje křížek a text view
        textView.setVisibility(INVISIBLE);
        findViewById(R.id.delete_search).setVisibility(INVISIBLE);
        hideKeyboard();
    }

    /**
     * otervře search bar a klávesnici
     */
    private void open() {
        open = true;
        GradientDrawable backgroundDrawable = (GradientDrawable) background.getBackground();
        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(backgroundDrawable, "cornerRadius", 100, 32);

        final ViewGroup.LayoutParams layoutParams = background.getLayoutParams();
        int parentWidth = findViewById(R.id.search_layout).getWidth();
        width = background.getWidth();
        ValueAnimator widthAnimator = ValueAnimator.ofInt(width, parentWidth);
        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.width = (int)animation.getAnimatedValue();
                background.setLayoutParams(layoutParams);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(cornerAnimation, widthAnimator);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.delete_search).setVisibility(VISIBLE);
                textView.setVisibility(VISIBLE);
                textView.requestFocus();
                showKeyboard();
            }
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    /**
     * zavře search bar, zasune klávesnici
     */
    private void close() {
        open = false;
        GradientDrawable backgroundDrawable = (GradientDrawable) background.getBackground();
        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(backgroundDrawable, "cornerRadius", 32, 100);

        final ViewGroup.LayoutParams layoutParams = background.getLayoutParams();
        int parentWidth = findViewById(R.id.search_layout).getWidth();
        ValueAnimator widthAnimator = ValueAnimator.ofInt(parentWidth, width);
        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.width = (int)animation.getAnimatedValue();
                background.setLayoutParams(layoutParams);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(cornerAnimation, widthAnimator);
        animatorSet.start();

        findViewById(R.id.delete_search).setVisibility(INVISIBLE);
        textView.setVisibility(INVISIBLE);
        hideKeyboard();
        textView.clearFocus();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
    }

    public void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public void setViewModel(SearchBarViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public static class SearchBarViewModel extends AndroidViewModel {

        public SearchBarViewModel(@NonNull Application application) {
            super(application);
        }
    }

}
