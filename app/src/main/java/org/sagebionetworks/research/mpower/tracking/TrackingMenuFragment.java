package org.sagebionetworks.research.mpower.tracking;

import static com.google.common.base.Preconditions.checkState;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.sagebionetworks.research.mpower.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TrackingMenuFragment extends Fragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingMenuFragment.class);

    private static final List<Integer> MEASURING_LABELS =
            Arrays.asList(R.string.measuring_center_label, R.string.measuring_left_label, R.string.measuring_right_label);

    private static final List<Integer> TRACKING_LABELS =
            Arrays.asList(R.string.tracking_center_label, R.string.tracking_left_label, R.string.tracking_right_label);

    // TODO for now icons have a white background, fix this when design gets the images without the background.
    private static final List<Integer> MEASURING_ICONS =
            Arrays.asList(R.drawable.walk_and_balance_icon, R.drawable.finger_tapping_icon, R.drawable.tremor_icon);

    private static final List<Integer> TRACKING_ICONS =
            Arrays.asList(R.drawable.medication_icon, R.drawable.symptom_icon, R.drawable.trigger_icon);

    // TODO get the gesture recognizer working.
    private class FlingListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event1.getY() < event2.getY()) {
                // we have a down fling
                setShowingContent(false);
            } else {
                setShowingContent(true);
            }

            return true;
        }
    }


    private static final int ANIMATION_DURATION = 150;
    @ColorRes
    private static final int SELECTED_COLOR = R.color.royal500;
    @ColorRes
    private static final int UNSELECTED_COLOR = R.color.royal400;
    private int selectedId;
    private boolean showingContent;

    @BindView(R.id.tracking_tab)
    TextView trackingButton;

    @BindView(R.id.measuring_tab)
    TextView measuringButton;

    @BindView(R.id.tracking_selected_image_view)
    ImageView trackingSelectedImageView;

    @BindView(R.id.measuring_selected_image_view)
    ImageView measuringSelectedImageView;

    @BindView(R.id.tracking_menu_content_view)
    View contentView;

    @BindViews({R.id.centerIconLabel, R.id.leftIconLabel, R.id.rightIconLabel})
    protected List<TextView> labels;

    @BindViews({R.id.centerIconImageView, R.id.leftIconImageView, R.id.rightIconImageView})
    protected List<ImageView> imageViews;

    private Unbinder unbinder;
    private GestureDetectorCompat gestureDetector;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.tracking_menu_fragment, group, false);
        this.unbinder = ButterKnife.bind(this, result);
        this.updateMenuState(true, false);
        this.setIcons(TRACKING_ICONS, TRACKING_LABELS);
        this.selectedId = this.trackingButton.getId();
        this.trackingButton.setOnClickListener(view -> updateSelection(R.id.tracking_tab));
        this.measuringButton.setOnClickListener(view -> updateSelection(R.id.measuring_tab));
        this.setIconListeners();
        this.showingContent = true;
        this.gestureDetector = new GestureDetectorCompat(this.getContext(), new FlingListener());
        result.setOnTouchListener((view, event) -> {
            view.performClick();
            return this.gestureDetector.onTouchEvent(event);
        });

        return result;
    }

    private void setIconListeners() {
        for (int i = 0; i < this.imageViews.size(); i++) {
            final int copy = i;
            this.imageViews.get(i).setOnClickListener(view -> {
                String selection = null;
                if (this.selectedId == R.id.tracking_tab) {
                    selection = this.getResources().getString(TRACKING_LABELS.get(copy));
                } else if (this.selectedId == R.id.measuring_tab) {
                    selection = this.getResources().getString(MEASURING_LABELS.get(copy));
                }

                if (selection != null) {
                    // TODO Do something with the button the user pressed;
                    LOGGER.debug("Icon " + selection.trim() + " was selected.");
                }
            });
        }
    }

    private void setIcons(List<Integer> icons, List<Integer> labels) {
        for (int i = 0; i < this.imageViews.size(); i++) {
            ImageView imageView = this.imageViews.get(i);
            TextView label = this.labels.get(i);
            imageView.setImageResource(icons.get(i));
            label.setText(labels.get(i));
        }
    }

    public void setShowingContent(boolean showingContent) {
        if (this.showingContent != showingContent) {
            this.showingContent = showingContent;
            int visibility = showingContent ? View.VISIBLE : View.GONE;
            this.contentView.setVisibility(visibility);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.unbinder.unbind();
    }


    private void updateSelection(int selectionId) {
        if (selectionId != this.selectedId) {
            this.setShowingContent(true);
            this.selectedId = selectionId;
            if (selectionId == R.id.tracking_tab) {
                this.setIcons(TRACKING_ICONS, TRACKING_LABELS);
                this.updateMenuState(true, false);
            } else if (selectionId == R.id.measuring_tab) {
                this.setIcons(MEASURING_ICONS, MEASURING_LABELS);
                this.updateMenuState(false, true);
            }
        } else {
            this.setShowingContent(false);
            this.selectedId = 0;
            this.updateMenuState(false, false);
        }
    }

    private void updateMenuState(boolean trackingSelected, boolean measuringSelected) {
        // It doesn't make sense for both to be selected.
        checkState(!(trackingSelected && measuringSelected));
        Resources resources = this.getResources();
        int trackingButtonColor = trackingSelected ? resources.getColor(SELECTED_COLOR) : resources.getColor(UNSELECTED_COLOR);
        int measuringButtonColor = measuringSelected ? resources.getColor(SELECTED_COLOR) : resources.getColor(UNSELECTED_COLOR);
        float trackingImageViewAlpha = trackingSelected ? 1f : 0f;
        float measuringImageViewAlpha = measuringSelected ? 1f : 0f;
        this.trackingButton.setTextColor(trackingButtonColor);
        this.measuringButton.setTextColor(measuringButtonColor);
        this.trackingSelectedImageView.setAlpha(trackingImageViewAlpha);
        this.measuringSelectedImageView.setAlpha(measuringImageViewAlpha);
    }
}
