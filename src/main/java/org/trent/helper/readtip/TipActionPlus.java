package org.trent.helper.readtip;

public class TipActionPlus extends AbstractTipAction {

    @Override
    protected String loadTip() {
        return ReadTipDispatcher.getTipContentPlus();
    }
}
