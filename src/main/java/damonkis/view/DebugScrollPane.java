package damonkis.view;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ScrollPaneSkin;

/**
 * Exists as a workaround for a reported bug with {@link ScrollPane}.
 *
 * <p>If a scrollbar has its policy set to {@code AS_NEEDED},
 * its size is not considered when calculation the ScrollPane's preferred size.
 * this results in a bit of the content being covered/squished whenever a scroll bar appears.</p>
 *
 * <p>The reported bug only addresses this behavior for the horizontal scroll bar,
 * but it is also present for the vertical scroll bar</p>
 *
 * @see <a href=https://stackoverflow.com/questions/49386416/javafx-scrollpanes-horizontal-scroll-bar-hides-content>Stack Overflow question with the workarround</a>
 * @see <a href=https://bugs.openjdk.org/browse/JDK-8199934> (JDK-8199934) ScrollPane: horizontal scrollBar hides content</a>
 */
public class DebugScrollPane extends ScrollPane {
    @Override
    protected Skin<?> createDefaultSkin() {
        return new DebugScrollPaneSkin(this);
    }

    private static class DebugScrollPaneSkin extends ScrollPaneSkin {
        public DebugScrollPaneSkin(ScrollPane scroll) {
            super(scroll);
            registerChangeListener(scroll.hbarPolicyProperty(),
                    p -> getHorizontalScrollBar().setVisible(false));
            registerChangeListener(scroll.vbarPolicyProperty(),
                    p -> getVerticalScrollBar().setVisible(false));
        }

        @Override
        protected double computePrefHeight(
                double width,
                double topInset,
                double rightInset,
                double bottomInset,
                double leftInset
        ) {
            double computed = super.computePrefHeight(
                    width, topInset, rightInset, bottomInset, leftInset);
            if (getSkinnable().getHbarPolicy() == ScrollBarPolicy.AS_NEEDED
                    && getHorizontalScrollBar().isVisible()) {
                computed += getHorizontalScrollBar().prefHeight(-1);
            }
            return computed;
        }

        @Override
        protected double computePrefWidth(
                double height,
                double topInset,
                double rightInset,
                double bottomInset,
                double leftInset
        ) {
            double computed = super.computePrefWidth(
                    height, topInset, rightInset, bottomInset, leftInset);
            if (getSkinnable().getVbarPolicy() == ScrollBarPolicy.AS_NEEDED
                    && getVerticalScrollBar().isVisible()) {
                computed += getVerticalScrollBar().prefWidth(-1);
            }
            return computed;
        }
    }
}
