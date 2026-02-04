package org.trent.helper.settings;

import java.awt.*;

public interface Setting {

    Component createComponent();

    boolean isModified();

    void reset();

    void apply();
}
