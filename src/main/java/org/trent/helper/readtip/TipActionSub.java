package org.trent.helper.readtip;

public class TipActionSub extends AbstractTipAction {

    @Override
    protected String loadTip() {
        return ReadTipDispatcher.getTipContentSub();
    }
}
