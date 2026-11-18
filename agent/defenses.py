import re

RECOMMEND_LINE = re.compile(r"(?m)RECOMMEND\s*[:：]\s*(.+)$")

CATEGORY_ALIASES = {
    "笔记本": "笔记本电脑",
    "电脑": "笔记本电脑",
    "手环": "智能穿戴",
    "手表": "智能穿戴",
    "耳机": "耳机音箱",
    "音箱": "耳机音箱",
    "音响": "耳机音箱",
    "平板": "平板电脑",
    "帐篷": "户外装备",
    "冲锋衣": "户外装备",
    "登山包": "户外装备",
    "睡袋": "户外装备",
    "相机": "相机摄影",
    "微单": "相机摄影",
    "游戏机": "游戏设备",
    "奶粉": "婴儿食品",
    "猫粮": "宠物食品",
    "狗粮": "宠物食品",
    "猫砂": "宠物用品",
    "积木": "婴童玩具",
    "剃须刀": "男士护理",
    "咖啡": "茶叶咖啡",
    "自行车": "骑行装备",
    "公路车": "骑行装备",
    "头盔": "骑行装备",
    "吹风机": "生活电器",
    "大米": "粮油调味",
}


def to_int(o):
    if isinstance(o, bool):
        return -1
    if isinstance(o, (int, float)):
        return int(o)
    try:
        return int(str(o))
    except Exception:
        return -1


def to_float(o):
    if isinstance(o, bool):
        return -1.0
    if isinstance(o, (int, float)):
        return float(o)
    try:
        return float(str(o))
    except Exception:
        return -1.0


def parse_budget(message):
    m = re.search(r"(\d{3,6})", message or "")
    if not m:
        return None
    v = float(m.group(1))
    if v < 100 or v > 100000:
        return None
    approx = any(w in message for w in ("左右", "大概", "大约", "差不多"))
    return v * 1.15 if approx else v


def category_matches(seg, category, goods_name):
    """品类片段匹配：分类名或商品名命中即可（商城分类较粗，如"户外装备"下同时有帐篷、登山包）。"""
    if not seg:
        return True
    return seg in str(category or "") or seg in str(goods_name or "")


def named_category_segment_in(query, cats):
    """识别用户点名的品类：优先叶子分类，其次一级分类，最后口语别名。

    别名命中时，若映射目标是商城真实分类就返回分类名（"电脑"→"笔记本电脑"），
    否则退回别名本身，避免"帐篷"被放大成"户外装备"导致跨品类推荐。
    """
    if not query or not query.strip():
        return None
    leaves = []
    parents = []
    for name in cats.values():
        parts = [p.strip() for p in str(name).split("/") if p.strip()]
        if not parts:
            continue
        if len(parts) == 1:
            parents.append(parts[0])
        else:
            leaves.append(parts[-1])
    for leaf in leaves:
        if len(leaf) >= 2 and leaf in query:
            return leaf
    for parent in parents:
        if len(parent) >= 2 and parent in query:
            return parent
    for alias, target in CATEGORY_ALIASES.items():
        if alias in query:
            # 映射目标是商城真实分类时用分类名检索，"电脑"就不会命中名字里带"电脑"的电脑包
            return target if (target in leaves or target in parents) else alias
    return None


def strip_recommend(text):
    return RECOMMEND_LINE.sub("", text or "").strip()


SHOP_INTENT = re.compile(r"买|购|推荐|好物|礼物|入手|挑|需要|想要|来点|有什么|有没有|便宜|实惠|性价比|划算|多少钱|价格|价位|预算")

# 售后类问题即使带了"有没有/多少钱"也不该去搜商品，避免答成"商城没有该商品"
_SERVICE_INTENT = re.compile(r"订单|物流|退换|退货|退款|优惠券|支付|发票|客服|售后")


def looks_shop_intent(text):
    """判断这句话是否表达了购物/推荐意图，用于强制补检索。"""
    t = text or ""
    if _SERVICE_INTENT.search(t):
        return False
    return bool(SHOP_INTENT.search(t))


PAIRING_RE = re.compile(
    r"([\u4e00-\u9fff]{2,8}?)\s*(?:适合|可以|能)?\s*(?:搭配|配合)\s*(?:什么|哪些|啥|点什么)"
)


def pairing_source(text):
    """识别"A 适合搭配什么"这类搭配咨询，返回主商品 A。

    这类问题的答案必须是能与 A 搭配的其他品类（问耳机就推手机、平板），
    A 本身不能出现在推荐里，否则就是答非所问。
    """
    m = PAIRING_RE.search(text or "")
    return m.group(1).strip() if m else None


_NUM_CHOICE = re.compile(
    r"^\s*(?:选|要|第)?\s*([1-9])(?![0-9])\s*(?:个|款|件|号)?\s*(?:就|要)?\s*(?:这个|它|这款|那款)?\s*(.*)$"
)
# 含价格/预算语义的句子不能当序号解析，否则"500元以内耳机"会被截成"序号5 + 00元以内耳机"
_PRICE_LIKE = re.compile(r"\d\s*(?:元|块|千|万)|以内|以上|以下|左右|预算")


def resolve_numeric_choice(raw, prev_assistant):
    """用户回复序号时还原成上一轮列表中的对应商品名。

    支持"1"、"第2个"这种纯序号；也支持"1手机"这类"序号+限定词"，
    此时以限定词为准——用户显式点名的品类/型号比序号更可靠，
    否则模型会把它当成上一轮商品的延续（例如"1手机"被答成手机搭配耳机）。
    """
    text = (raw or "").strip()
    if _PRICE_LIKE.search(text):
        return None
    m = _NUM_CHOICE.match(text)
    if not m:
        return None
    idx = int(m.group(1))
    tail = re.sub(r"[^\u4e00-\u9fff0-9a-zA-Z]", "", m.group(2) or "")
    if len(tail) >= 2:
        return tail
    if not prev_assistant:
        return None
    items = re.findall(r"(?m)^\s*(?:[\(（]?([1-9])[\)）\.、:：]\s+)(.+)", prev_assistant)
    text = {int(no): body.strip() for no, body in items}.get(idx)
    if not text:
        return None
    name = re.split(r"[：:，,。；;（(]", text)[0].strip()
    return name or None



_ORDINAL = re.compile(
    r"^\s*(?:选|要|那就|就|还是)?\s*(?:第\s*)?([1-9]|[一二两三四五六七八九十])(?![0-9])\s*(?:个|款|件|号|台|部)?\s*(?:吧|呢|的)?\s*(.*)$"
)
_CN_NUM = {"一": 1, "二": 2, "两": 2, "三": 3, "四": 4, "五": 5, "六": 6, "七": 7, "八": 8, "九": 9, "十": 10}
# 序号后面的追问词，不属于"限定品类"，不能因此放弃序号解析
_ORDINAL_SUFFIX = re.compile(
    r"^(多少钱|价格|价钱|怎么样|如何|详情|参数|配置|库存|有货|评价|贵不贵|值不值|值得|看看|介绍|吗|呢|吧|的)+"
)


def ordinal_index(raw):
    """解析用户这句话里的序号：1-based 序号，0 表示"最后一个"，None 表示不是序号。

    带限定词（例如"1手机"、"2那个耳机"）时不按序号处理，返回 None。
    """
    text = (raw or "").strip()
    if not text or _PRICE_LIKE.search(text):
        return None
    if re.match(r"^\s*最后\s*(?:一)?\s*(?:个|款|件|号)?\s*$", text):
        return 0
    m = _ORDINAL.match(text)
    if not m:
        return None
    token = m.group(1)
    tail = re.sub(r"[^\u4e00-\u9fff0-9a-zA-Z]", "", m.group(2) or "")
    tail = _ORDINAL_SUFFIX.sub("", tail)
    if len(tail) >= 2:
        return None
    return _CN_NUM.get(token) or int(token)


def resolve_ordinal(raw, goods_list):
    """从"第2个/第二个/2号"解析出本轮指向的推荐商品。

    依赖持久化的最近推荐列表，而不是上一轮 AI 文本，这样连续追问序号时
    始终指向同一批商品；只有出现新的推荐列表才切换基准。
    带限定词（例如"1手机"）时不走序号，交给原检索流程。
    """
    if not goods_list:
        return None
    idx = ordinal_index(raw)
    if idx is None:
        return None
    if idx == 0:
        return goods_list[-1]
    if 1 <= idx <= len(goods_list):
        return goods_list[idx - 1]
    return None


def recommend_ids(text):
    m = RECOMMEND_LINE.search(text or "")
    if not m:
        return []
    ids = []
    seen = set()
    for s in re.findall(r"\d+", m.group(1)):
        i = int(s)
        if i in seen:
            continue
        seen.add(i)
        ids.append(i)
    return ids

