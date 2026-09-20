package org.trent.helper.readtip;

import org.trent.helper.readtip.common.ReadTipState;
import org.trent.helper.readtip.common.SharedUtils;
import org.trent.helper.readtip.local.LocalSourceHandler;
import org.trent.helper.readtip.remote.RemoteSourceHandler;

/**
 * 翻页入口调度：根据书源类型分发到 RemoteSourceHandler 或 LocalSourceHandler
 */
public class ReadTipDispatcher {

    public static String getTipContentPlus() {
        ReadTipState state = ReadTipState.getInstance();
        if (SharedUtils.isLocalSource(state)) {
            try {
                return LocalSourceHandler.nextPage(state);
            } catch (Exception e) {
                return SharedUtils.friendlyMessage(e, "当前数据源暂时无法加载内容");
            }
        }
        if (!SharedUtils.checkPageTurnSpeed()) {
            return "翻页太快了，请慢一点..";
        }
        try {
            return RemoteSourceHandler.nextPage(state);
        } catch (Exception e) {
            return SharedUtils.friendlyMessage(e, "当前数据源暂时无法加载内容");
        }
    }

    public static String getTipContentSub() {
        ReadTipState state = ReadTipState.getInstance();
        if (SharedUtils.isLocalSource(state)) {
            try {
                return LocalSourceHandler.prevPage(state);
            } catch (Exception e) {
                return SharedUtils.friendlyMessage(e, "当前数据源暂时无法加载内容");
            }
        }
        if (!SharedUtils.checkPageTurnSpeed()) {
            return "翻页太快了，请慢一点..";
        }
        try {
            return RemoteSourceHandler.prevPage(state);
        } catch (Exception e) {
            return SharedUtils.friendlyMessage(e, "当前数据源暂时无法加载内容");
        }
    }

    public static String getTipContent() {
        return getTipContent(ReadTipState.getInstance());
    }

    public static String getTipContent(ReadTipState state) {
        return getTipContent(state, true);
    }

    public static String getTipContent(ReadTipState state, boolean applyRateLimit) {
        if (applyRateLimit && !SharedUtils.isLocalSource(state) && !SharedUtils.checkPageTurnSpeed()) {
            return "翻页太快了，请慢一点..";
        }
        try {
            ensureContentLoaded(state);
        } catch (Exception e) {
            return SharedUtils.friendlyMessage(e, "无可用内容");
        }
        if (state.index >= 0 && state.index < state.textList.size()) {
            return state.textList.get(state.index);
        }
        return "无可用内容";
    }

    public static void ensureContentLoaded(ReadTipState state) {
        if (state.textList.isEmpty()) {
            if (SharedUtils.isLocalSource(state)) {
                LocalSourceHandler.ensureContentLoaded(state);
            } else {
                RemoteSourceHandler.ensureContentLoaded(state);
            }
        }
    }

    public static boolean isLocalSource(ReadTipState state) {
        return SharedUtils.isLocalSource(state);
    }
}
