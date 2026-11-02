import math
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

    别名命中时返回别名本身而不是映射的父类，避免"帐篷"被放大成"户外装备"导致跨品类推荐。
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
    for alias in CATEGORY_ALIASES:
        if alias in query:
            return alias
    return None


def strip_recommend(text):
    return RECOMMEND_LINE.sub("", text or "").strip()


SHOP_INTENT = re.compile(r"买|购|推荐|好物|礼物|入手|挑|需要|想要|来点|有什么")


def looks_shop_intent(text):
    """判断这句话是否表达了购物/推荐意图，用于强制补检索。"""
    return bool(SHOP_INTENT.search(text or ""))


def resolve_numeric_choice(raw, prev_assistant):
    """用户只回复序号（"1"、"第2个"、"选3"）时，还原成上一轮列表中的对应商品名。"""
    if not prev_assistant:
        return None
    m = re.fullmatch(r"\s*(?:选|要|第)?\s*([1-9])\s*(?:个|款|件|号)?\s*(?:就|要)?(?:这个|它|这款|那款)?\s*", raw or "")
    if not m:
        return None
    idx = int(m.group(1))
    items = re.findall(r"(?m)^\s*(?:[\(（]?([1-9])[\)）\.、:：]\s+)(.+)", prev_assistant)
    text = {int(no): body.strip() for no, body in items}.get(idx)
    if not text:
        return None
    name = re.split(r"[：:，,。；;（(]", text)[0].strip()
    return name or None



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


def fuzzy_name_match(reply, name):
    n, m = len(name), len(reply)
    if n < 4 or m == 0:
        return False
    min_len = max(4, math.ceil(n * 0.55))
    best = 0
    prev = [0] * (m + 1)
    for i in range(1, n + 1):
        cur = [0] * (m + 1)
        ci = name[i - 1]
        for j in range(1, m + 1):
            if ci == reply[j - 1]:
                cur[j] = prev[j - 1] + 1
                if cur[j] > best:
                    best = cur[j]
        prev = cur
    return best >= min_len


def guard_prompt_text(message, seg, goods_json):
    return (message + "\n[系统注：用户点名的商品类型是「" + seg + "」。\n"
            + "商城在售该类型的商品（真实数据）：\n" + goods_json + "\n"
            + "以上商品可直接作为推荐依据；如果工具返回了更多同类型商品，也可以一并参考。"
            + "推荐里只能出现工具返回过的商品；若没有合适的，如实说明并礼貌追问需求，不要改推其他类型商品]")
