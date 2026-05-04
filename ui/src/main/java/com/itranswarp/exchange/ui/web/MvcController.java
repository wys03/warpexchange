package com.itranswarp.exchange.ui.web;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.exchange.ApiException;
import com.itranswarp.exchange.bean.AuthToken;
import com.itranswarp.exchange.bean.TransferRequestBean;
import com.itranswarp.exchange.client.RestClient;
import com.itranswarp.exchange.ctx.UserContext;
import com.itranswarp.exchange.enums.AssetEnum;
import com.itranswarp.exchange.enums.UserType;
import com.itranswarp.exchange.model.ui.UserProfileEntity;
import com.itranswarp.exchange.support.LoggerSupport;
import com.itranswarp.exchange.user.UserService;
import com.itranswarp.exchange.util.HashUtil;

@Controller
public class MvcController extends LoggerSupport {

    @Value("#{exchangeConfiguration.hmacKey}")
    String hmacKey;

    @Autowired
    CookieService cookieService;

    @Autowired
    UserService userService;

    @Autowired
    RestClient restClient;

    @Autowired
    Environment environment;

    @PostConstruct
    public void init() {
        // 本地开发环境下自动创建用户user0@example.com ~ user9@example.com:
        if (isLocalDevEnv()) {
            for (int i = 0; i <= 9; i++) {
                String email = "user" + i + "@example.com";
                String name = "User-" + i;
                String password = "password" + i;
                if (userService.fetchUserProfileByEmail(email) == null) {
                    logger.info("auto create user {} for local dev env...", email);
                    doSignup(email, name, password);
                }
            }
        }
    }

    /**
     * Index page.
     */
    @GetMapping("/")
    public ModelAndView index() {
        return prepareModelAndView("home", buildPortalHomeModel());
    }

    @GetMapping("/trade")
    public ModelAndView trade() {
        if (UserContext.getUserId() == null) {
            return redirect("/signin");
        }
        return prepareModelAndView("index");
    }

    @GetMapping("/news/{id}")
    public ModelAndView newsDetail(@PathVariable("id") String id) {
        if (UserContext.getUserId() == null) {
            return redirect("/signin");
        }
        Map<String, Object> portal = buildPortalHomeModel();
        Map<String, Object> news = findNewsById(portal, id);
        if (news == null) {
            return redirect("/");
        }
        return prepareModelAndView("news_detail", Map.of(
                "news", news,
                "relatedNews", collectRelatedNews(portal, id)));
    }

    @GetMapping("/wallet")
    public ModelAndView wallet() {
        if (UserContext.getUserId() == null) {
            return redirect("/signin");
        }
        return prepareModelAndView("wallet");
    }

    @GetMapping("/signup")
    public ModelAndView signup() {
        if (UserContext.getUserId() != null) {
            return redirect("/");
        }
        return prepareModelAndView("signup");
    }

    @PostMapping("/signup")
    public ModelAndView signup(@RequestParam("email") String email, @RequestParam("name") String name,
            @RequestParam("password") String password) {
        // check email:
        if (email == null || email.isBlank()) {
            return prepareModelAndView("signup", Map.of("email", email, "name", name, "error", "Invalid email."));
        }
        email = email.strip().toLowerCase();
        if (email.length() > 100 || !EMAIL.matcher(email).matches()) {
            return prepareModelAndView("signup", Map.of("email", email, "name", name, "error", "Invalid email."));
        }
        if (userService.fetchUserProfileByEmail(email) != null) {
            return prepareModelAndView("signup", Map.of("email", email, "name", name, "error", "Email exists."));
        }
        // check name:
        if (name == null || name.isBlank() || name.strip().length() > 100) {
            return prepareModelAndView("signup", Map.of("email", email, "name", name, "error", "Invalid name."));
        }
        name = name.strip();
        // check password:
        if (password == null || password.length() < 8 || password.length() > 32) {
            return prepareModelAndView("signup", Map.of("email", email, "name", name, "error", "Invalid password."));
        }
        doSignup(email, name, password);
        return redirect("/signin");
    }

    @PostMapping(value = "/websocket/token", produces = "application/json")
    @ResponseBody
    String requestWebSocketToken() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            // 无登录信息，返回JSON空字符串"":
            return "\"\"";
        }
        // 1分钟后过期:
        AuthToken token = new AuthToken(userId, System.currentTimeMillis() + 60_000);
        String strToken = token.toSecureString(hmacKey);
        // 返回JSON字符串"xxx":
        return "\"" + strToken + "\"";
    }

    private UserProfileEntity doSignup(String email, String name, String password) {
        // sign up:
        UserProfileEntity profile = userService.signup(email, name, password);
        // 本地开发环境下自动给用户增加资产:
        if (isLocalDevEnv()) {
            logger.warn("auto deposit assets for user {} in local dev env...", profile.email);
            Random random = new Random(profile.userId);
            deposit(profile.userId, AssetEnum.BTC, new BigDecimal(random.nextInt(5_00, 10_00)).movePointLeft(2));
            deposit(profile.userId, AssetEnum.USD,
                    new BigDecimal(random.nextInt(100000_00, 400000_00)).movePointLeft(2));
        }
        logger.info("user signed up: {}", profile);
        return profile;
    }

    private boolean isLocalDevEnv() {
        return environment.getActiveProfiles().length == 0
                && Arrays.equals(environment.getDefaultProfiles(), new String[] { "default" });
    }

    private void deposit(Long userId, AssetEnum asset, BigDecimal amount) {
        var req = new TransferRequestBean();
        req.transferId = HashUtil.sha256(userId + "/" + asset + "/" + amount.stripTrailingZeros().toPlainString())
                .substring(0, 32);
        req.amount = amount;
        req.asset = asset;
        req.fromUserId = UserType.DEBT.getInternalUserId();
        req.toUserId = userId;
        restClient.post(Map.class, "/internal/transfer", null, req);
    }

    @GetMapping("/signin")
    public ModelAndView signin(HttpServletRequest request) {
        if (UserContext.getUserId() != null) {
            return redirect("/");
        }
        return prepareModelAndView("signin");
    }

    /**
     * Do sign in.
     */
    @PostMapping("/signin")
    public ModelAndView signIn(@RequestParam("email") String email, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response) {
        if (email == null || email.isEmpty()) {
            return prepareModelAndView("signin", Map.of("email", email, "error", "Invalid email or password."));
        }
        if (password == null || password.isEmpty()) {
            return prepareModelAndView("signin", Map.of("email", email, "error", "Invalid email or password."));
        }
        email = email.toLowerCase();
        try {
            UserProfileEntity userProfile = userService.signin(email, password);
            // sign in ok and set cookie:
            AuthToken token = new AuthToken(userProfile.userId,
                    System.currentTimeMillis() + 1000 * cookieService.getExpiresInSeconds());
            cookieService.setSessionCookie(request, response, token);
        } catch (ApiException e) {
            logger.warn("sign in failed for " + e.getMessage(), e);
            return prepareModelAndView("signin", Map.of("email", email, "error", "Invalid email or password."));
        } catch (Exception e) {
            logger.warn("sign in failed for " + e.getMessage(), e);
            return prepareModelAndView("signin", Map.of("email", email, "error", "Internal server error."));
        }

        logger.info("signin ok.");
        return redirect("/");
    }

    @GetMapping("/signout")
    public ModelAndView signout(HttpServletRequest request, HttpServletResponse response) {
        cookieService.deleteSessionCookie(request, response);
        return redirect("/");
    }

    // util method:

    ModelAndView prepareModelAndView(String view, Map<String, Object> model) {
        ModelAndView mv = new ModelAndView(view);
        mv.addAllObjects(model);
        addGlobalModel(mv);
        return mv;
    }

    ModelAndView prepareModelAndView(String view) {
        ModelAndView mv = new ModelAndView(view);
        addGlobalModel(mv);
        return mv;
    }

    ModelAndView prepareModelAndView(String view, String key, Object value) {
        ModelAndView mv = new ModelAndView(view);
        mv.addObject(key, value);
        addGlobalModel(mv);
        return mv;
    }

    void addGlobalModel(ModelAndView mv) {
        final Long userId = UserContext.getUserId();
        mv.addObject("__userId__", userId);
        mv.addObject("__profile__", userId == null ? null : userService.getUserProfile(userId));
        mv.addObject("__time__", Long.valueOf(System.currentTimeMillis()));
    }

    ModelAndView notFound() {
        ModelAndView mv = new ModelAndView("404");
        addGlobalModel(mv);
        return mv;
    }

    ModelAndView redirect(String url) {
        return new ModelAndView("redirect:" + url);
    }

    Map<String, Object> buildPortalHomeModel() {
        return Map.of(
                "marketOverview", List.of(
                        Map.of("name", "BTC/USD", "last", "72352.00", "change", "+2.31%", "trendClass", "up"),
                        Map.of("name", "ETH/USD", "last", "3628.40", "change", "+1.08%", "trendClass", "up"),
                        Map.of("name", "NASDAQ", "last", "18342.51", "change", "-0.42%", "trendClass", "down"),
                        Map.of("name", "DXY", "last", "104.23", "change", "+0.17%", "trendClass", "up")),
                "hotSectors", List.of(
                        Map.of("name", "AI 算力", "heat", "94", "lead", "NVIDIA"),
                        Map.of("name", "加密合规", "heat", "88", "lead", "COIN"),
                        Map.of("name", "贵金属", "heat", "82", "lead", "XAUUSD")),
                "fundFlow", List.of(
                        Map.of("name", "主力净流入", "value", "+18.6M", "trendClass", "up"),
                        Map.of("name", "大单净流入", "value", "+7.2M", "trendClass", "up"),
                        Map.of("name", "散户净流入", "value", "-5.1M", "trendClass", "down")),
                "rankings", List.of(
                        Map.of("symbol", "BTCUSD", "price", "72352.00", "change", "+2.31%", "trendClass", "up"),
                        Map.of("symbol", "ETHUSD", "price", "3628.40", "change", "+1.08%", "trendClass", "up"),
                        Map.of("symbol", "XAUUSD", "price", "2398.77", "change", "-0.35%", "trendClass", "down")),
                "headlineNews", List.of(
                        news("head-001", "09:30", "三大指数高开分化，成交结构向科技成长倾斜。", "市场要闻", "盘初"),
                        news("head-002", "09:18", "主流币成交回暖，盘中波动率边际回落。", "数字资产", "链上"),
                        news("head-003", "09:05", "美元指数维持强势，贵金属高位震荡。", "宏观观察", "外汇")),
                "analysisNews", List.of(
                        news("analysis-001", "10:02", "量价配合改善，短线情绪修复仍需成交确认。", "技术分析", "解读"),
                        news("analysis-002", "09:46", "利率预期扰动下，成长与价值风格再平衡。", "宏观策略", "解读"),
                        news("analysis-003", "09:27", "ETF 资金流向转正，风险偏好出现边际回暖。", "资金面", "解读")),
                "flashNews", List.of(
                        news("flash-001", "09:35", "市场量能回暖，成长板块活跃度抬升。", "Warp 资讯", "快讯"),
                        news("flash-002", "09:20", "纳指期货翻红，科技龙头盘前走稳。", "Global Feed", "快讯"),
                        news("flash-003", "09:12", "美元指数震荡偏强，贵金属高位反复。", "Macro Watch", "快讯")),
                "policyWatch", List.of(
                        news("policy-001", "08:40", "监管层发布跨境支付试点新进展，市场关注实施细则。", "政策跟踪", "政策"),
                        news("policy-002", "08:12", "数据要素流通配套办法征求意见，科技板块反应积极。", "政策跟踪", "政策"),
                        news("policy-003", "07:55", "能源相关补贴政策窗口临近，产业链关注度提升。", "政策跟踪", "政策")),
                "calendar", List.of(
                        Map.of("time", "20:30", "event", "美国初请失业金人数", "level", "高"),
                        Map.of("time", "22:00", "event", "美国成屋销售", "level", "中"),
                        Map.of("time", "次日02:00", "event", "美联储官员讲话", "level", "高")));
    }

    Map<String, Object> news(String id, String time, String title, String source, String tag) {
        return Map.of(
                "id", id,
                "time", time,
                "title", title,
                "source", source,
                "tag", tag,
                "author", "Warp 研究院",
                "publishedAt", "2026-04-21 " + time,
                "keywords", List.of(tag, "市场", "资讯"),
                "contentHtml", "<p>" + title + "</p>"
                        + "<p>从盘面结构看，资金更偏向流动性和基本面兼具的方向，短线交易建议关注成交量变化与关键支撑位表现。</p>"
                        + "<p>中期来看，宏观变量仍会影响风险偏好，建议结合仓位管理、波动控制和分散配置来优化回撤表现。</p>");
    }

    Map<String, Object> findNewsById(Map<String, Object> portal, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (Object key : List.of("flashNews", "headlineNews", "analysisNews", "policyWatch")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) portal.get(key.toString());
            for (Map<String, Object> row : list) {
                if (id.equals(row.get("id"))) {
                    return row;
                }
            }
        }
        return null;
    }

    List<Map<String, Object>> collectRelatedNews(Map<String, Object> portal, String id) {
        List<Map<String, Object>> all = new java.util.ArrayList<>();
        for (Object key : List.of("headlineNews", "analysisNews", "policyWatch", "flashNews")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) portal.get(key.toString());
            all.addAll(list);
        }
        return all.stream()
                .filter(m -> !id.equals(m.get("id")))
                .limit(6)
                .toList();
    }

    static Pattern EMAIL = Pattern.compile("^[a-z0-9\\-\\.]+\\@([a-z0-9\\-]+\\.){1,3}[a-z]{2,20}$");
}
