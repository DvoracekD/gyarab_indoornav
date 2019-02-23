package cz.gyarab.nav.modules;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import cz.gyarab.nav.R;

public class SearchBar extends ConstraintLayout {

    private AutoCompleteTextView textView;
    private OptionSelectedListener listener;

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

        //kliknutí na křížek
        findViewById(R.id.delete_search).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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

                textView.requestFocus();
                //otevřít klávesnici
                InputMethodManager inputMethodManager =
                        (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(textView.getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0);
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
            }
        });

    }

    public void hideKeyboard() {
        if (!hasFocus())return;
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

}
