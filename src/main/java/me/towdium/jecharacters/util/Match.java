package me.towdium.jecharacters.util;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mcp.MethodsReturnNonnullByDefault;
import me.towdium.jecharacters.JechConfig;
import me.towdium.jecharacters.core.JechCore;
import me.towdium.pinin.DictLoader;
import me.towdium.pinin.PinIn;
import me.towdium.pinin.searchers.TreeSearcher;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import net.minecraft.client.util.SuffixArray;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.towdium.pinin.searchers.Searcher.Logic.CONTAIN;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = JechCore.MODID)
public class Match {
    public static final PinIn context = new PinIn(new Loader()).config().accelerate(true).commit();
    static final Pattern p = Pattern.compile("a");
    static Set<TreeSearcher<?>> searchers = Collections.newSetFromMap(new WeakHashMap<>());

    public static <T> TreeSearcher<T> searcher() {
        TreeSearcher<T> ret = new TreeSearcher<>(CONTAIN, context);
        searchers.add(ret);
        return ret;
    }

    // Psi
    public static int rank(Object o, String s1, String s2) {
        return contains(s1, s2) ? 1 : 0;
    }

    @SuppressWarnings("unused")
    public static String wrap(String s) {
        return JechConfig.enableForceQuote ? '"' + s + '"' : s;
    }

    public static boolean contains(String s, CharSequence cs) {
        boolean b = context.contains(s, cs.toString());
        if (JechConfig.enableVerbose)
            JechCore.LOG.info("contains(" + s + ',' + cs + ")->" + b);
        return b;
    }

    public static boolean contains(CharSequence a, CharSequence b, boolean c) {
        if (c) return contains(a.toString().toLowerCase(), b.toString().toLowerCase());
        else return contains(a, b);
    }

    public static boolean equals(String s, Object o) {
        boolean b = o instanceof String && context.matches(s, (String) o);
        if (JechConfig.enableVerbose)
            JechCore.LOG.info("contains(" + s + ',' + o + ")->" + b);
        return b;
    }

    public static boolean contains(CharSequence a, CharSequence b) {
        return contains(a.toString(), b);
    }

    public static Matcher matcher(Pattern test, CharSequence name) {
        boolean result;
        if ((test.flags() & Pattern.CASE_INSENSITIVE) != 0 || (test.flags() & Pattern.UNICODE_CASE) != 0) {
            result = matches(name.toString().toLowerCase(), test.toString().toLowerCase());
        } else {
            result = matches(name.toString(), test.toString());
        }
        return result ? p.matcher("a") : p.matcher("");
    }

    public static boolean matches(String s1, String s2) {
        boolean start = s2.startsWith(".*");
        boolean end = s2.endsWith(".*");
        if (start && end && s2.length() < 4) end = false;
        if (start || end) s2 = s2.substring(start ? 2 : 0, s2.length() - (end ? 2 : 0));
        return contains(s1, s2);
    }

    public static boolean matches(String s1, CharSequence cs) {
        return matches(s1, cs.toString());
    }

    public static void onConfigChange() {
        context.config().keyboard(JechConfig.keyboard.get())
                .fAng2An(JechConfig.enableFuzzyAng2an).fEng2En(JechConfig.enableFuzzyEng2en)
                .fIng2In(JechConfig.enableFuzzyIng2in).fZh2Z(JechConfig.enableFuzzyZh2z)
                .fCh2C(JechConfig.enableFuzzyCh2c).fSh2S(JechConfig.enableFuzzySh2s)
                .fU2V(JechConfig.enableFuzzyU2v).commit();
        searchers.forEach(TreeSearcher::refresh);
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class FakeTree extends GeneralizedSuffixTree {
        TreeSearcher<Integer> tree = searcher();
        int highestIndex = -1;

        @Override
        public IntSet search(String word) {
            if (JechConfig.enableVerbose)
                JechCore.LOG.info("FakeTree:search(" + word + ')');
            return new IntOpenHashSet(tree.search(word));
        }

        @Override
        public void put(String key, int index) throws IllegalStateException {
            if (JechConfig.enableVerbose)
                JechCore.LOG.info("FakeTree:put(" + key + ',' + index + ')');
            if (index < highestIndex) {
                String err = "The input index must not be less than any of the previously " +
                        "inserted ones. Got " + index + ", expected at least " + highestIndex;
                throw new IllegalStateException(err);
            } else highestIndex = index;
            tree.put(key, index);
        }

        @Override
        public int getHighestIndex() {
            if (JechConfig.enableVerbose)
                JechCore.LOG.info("FakeTree:getHighestIndex()->" + highestIndex);
            return highestIndex;
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class FakeArray<T> extends SuffixArray<T> {
        TreeSearcher<T> tree = searcher();

        @Override
        public void add(T v, String k) {
            if (JechConfig.enableVerbose)
                JechCore.LOG.info("FakeArray:put(" + v + ',' + k + ')');
            tree.put(k, v);
        }

        @Override
        public void generate() {
        }

        @Override
        public List<T> search(String k) {
            if (JechConfig.enableVerbose)
                JechCore.LOG.info("FakeArray:search(" + k + ')');
            return tree.search(k);
        }

    }

    static class Loader extends DictLoader.Default {
        @Override
        public void load(BiConsumer<Character, String[]> feed) {
            super.load(feed);
            feed.accept('\u9FCF', new String[]{"mai4"});   // 钅麦
            feed.accept('\u9FD4', new String[]{"ge1"});    // 钅哥
            feed.accept('\u9FED', new String[]{"ni3"});    // 钅尔
            feed.accept('\u9FEC', new String[]{"tian2"});  // 石田
            feed.accept('\u9FEB', new String[]{"ao4"});    // 奥气
            feed.accept('\uE900', new String[]{"lu2"});    // 钅卢
            feed.accept('\uE901', new String[]{"du4"});    // 钅杜
            feed.accept('\uE902', new String[]{"xi3"});    // 钅喜
            feed.accept('\uE903', new String[]{"bo1"});    // 钅波
            feed.accept('\uE904', new String[]{"hei1"});   // 钅黑
            feed.accept('\uE906', new String[]{"da2"});    // 钅达
            feed.accept('\uE907', new String[]{"lun2"});   // 钅仑
            feed.accept('\uE910', new String[]{"fu1"});    // 钅夫
            feed.accept('\uE912', new String[]{"li4"});    // 钅立
        }
    }

}
