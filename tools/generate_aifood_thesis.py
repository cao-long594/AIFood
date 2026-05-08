from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(r"D:\Soft\AndroidProjects\AIFood")
OUT = ROOT / "AIFood-本科毕业论文.docx"
FIG_DIR = ROOT / "thesis_figures"


def cn_font(size=28, bold=False):
    candidates = [
        r"C:\Windows\Fonts\msyhbd.ttc" if bold else r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simsun.ttc",
        r"C:\Windows\Fonts\simhei.ttf",
    ]
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def wrap_text(text, font, max_width):
    lines, line = [], ""
    dummy = Image.new("RGB", (10, 10))
    draw = ImageDraw.Draw(dummy)
    for ch in text:
        test = line + ch
        if draw.textbbox((0, 0), test, font=font)[2] <= max_width:
            line = test
        else:
            if line:
                lines.append(line)
            line = ch
    if line:
        lines.append(line)
    return lines


def draw_center_text(draw, box, text, font, fill="#1f2937"):
    x1, y1, x2, y2 = box
    lines = wrap_text(text, font, x2 - x1 - 24)
    line_h = font.size + 8
    total_h = line_h * len(lines)
    y = y1 + ((y2 - y1) - total_h) / 2
    for line in lines:
        bbox = draw.textbbox((0, 0), line, font=font)
        x = x1 + ((x2 - x1) - (bbox[2] - bbox[0])) / 2
        draw.text((x, y), line, font=font, fill=fill)
        y += line_h


def arrow(draw, start, end, fill="#334155", width=4):
    draw.line([start, end], fill=fill, width=width)
    ex, ey = end
    sx, sy = start
    if abs(ex - sx) >= abs(ey - sy):
        sign = 1 if ex > sx else -1
        pts = [(ex, ey), (ex - sign * 14, ey - 8), (ex - sign * 14, ey + 8)]
    else:
        sign = 1 if ey > sy else -1
        pts = [(ex, ey), (ex - 8, ey - sign * 14), (ex + 8, ey - sign * 14)]
    draw.polygon(pts, fill=fill)


def rounded(draw, box, text, font, fill="#f8fafc", outline="#2563eb"):
    draw.rounded_rectangle(box, radius=18, fill=fill, outline=outline, width=3)
    draw_center_text(draw, box, text, font)


def make_canvas(title, w=1500, h=900):
    img = Image.new("RGB", (w, h), "white")
    draw = ImageDraw.Draw(img)
    title_font = cn_font(34, bold=True)
    draw.text((50, 34), title, font=title_font, fill="#111827")
    draw.line((50, 86, w - 50, 86), fill="#94a3b8", width=2)
    return img, draw


def save_flow(path, title, nodes):
    img, draw = make_canvas(title, 1500, 760)
    font = cn_font(25)
    x, y, bw, bh, gap = 90, 180, 210, 120, 52
    boxes = []
    for i, node in enumerate(nodes):
        box = (x + i * (bw + gap), y, x + i * (bw + gap) + bw, y + bh)
        rounded(draw, box, node, font)
        boxes.append(box)
    for a, b in zip(boxes, boxes[1:]):
        arrow(draw, (a[2], (a[1] + a[3]) // 2), (b[0], (b[1] + b[3]) // 2))
    img.save(path)


def save_use_case(path, detail=False):
    img, draw = make_canvas("AIFood 系统用例图" if not detail else "AIFood 系统细化用例图", 1500, 960)
    font = cn_font(24)
    small = cn_font(21)
    draw.ellipse((80, 210, 150, 280), outline="#111827", width=4)
    draw.line((115, 280, 115, 420), fill="#111827", width=4)
    draw.line((55, 330, 175, 330), fill="#111827", width=4)
    draw.line((115, 420, 60, 530), fill="#111827", width=4)
    draw.line((115, 420, 170, 530), fill="#111827", width=4)
    draw.text((72, 560), "普通用户", font=font, fill="#111827")
    draw.rounded_rectangle((280, 150, 1400, 850), radius=28, outline="#475569", width=3)
    draw.text((650, 165), "AIFood", font=font, fill="#111827")
    items = [
        ("查看营养分析", 420, 260), ("管理餐次记录", 760, 260), ("维护食物库", 1100, 260),
        ("记录饮水", 420, 500), ("查看历史数据", 760, 500), ("食物识别录入", 1100, 500),
    ]
    if detail:
        items += [("设置营养目标", 420, 700), ("编辑/删除记录", 760, 700), ("确认识别草稿", 1100, 700)]
    for text, cx, cy in items:
        draw.ellipse((cx - 120, cy - 52, cx + 120, cy + 52), fill="#eff6ff", outline="#2563eb", width=3)
        draw_center_text(draw, (cx - 120, cy - 52, cx + 120, cy + 52), text, small)
        draw.line((180, 370, cx - 120, cy), fill="#64748b", width=2)
    img.save(path)


def save_er(path, detail=False):
    img, draw = make_canvas("AIFood 核心实体 E-R 图" if not detail else "AIFood 识别与目标扩展 E-R 图", 1500, 920)
    font = cn_font(23)
    entities = [
        ("Food\n食物", 160, 180, ["id", "名称", "分类", "单位", "热量/营养素"]),
        ("MealRecord\n餐次记录", 600, 180, ["id", "日期", "餐次", "摄入量", "营养快照"]),
        ("UserGoal\n用户目标", 1040, 180, ["热量目标", "碳水目标", "蛋白质目标", "脂肪目标"]),
    ] if not detail else [
        ("RecognitionDraft\n识别草稿", 180, 190, ["食物名称", "候选置信度", "估计营养"]),
        ("FoodCandidate\n识别候选", 620, 190, ["名称", "分类", "每100g营养"]),
        ("WaterRecord\n饮水记录", 1060, 190, ["日期", "饮水量", "目标进度"]),
    ]
    boxes = []
    for title, x, y, attrs in entities:
        box = (x, y, x + 260, y + 280)
        draw.rounded_rectangle(box, radius=16, fill="#f8fafc", outline="#0f766e", width=3)
        draw_center_text(draw, (x, y + 10, x + 260, y + 80), title, font)
        draw.line((x + 20, y + 92, x + 240, y + 92), fill="#94a3b8", width=2)
        for i, attr in enumerate(attrs):
            draw.text((x + 36, y + 112 + i * 34), attr, font=cn_font(20), fill="#1f2937")
        boxes.append(box)
    arrow(draw, (boxes[0][2], 320), (boxes[1][0], 320))
    arrow(draw, (boxes[1][2], 320), (boxes[2][0], 320))
    draw.text((460, 285), "被选择生成", font=cn_font(21), fill="#475569")
    draw.text((900, 285), "参与统计对比", font=cn_font(21), fill="#475569")
    img.save(path)


def save_dfd(path, level2=False):
    img, draw = make_canvas("AIFood 顶层数据流图" if not level2 else "AIFood 记录与分析数据流图", 1500, 900)
    font = cn_font(23)
    if not level2:
        rounded(draw, (80, 360, 280, 480), "用户", font, fill="#f1f5f9", outline="#475569")
        rounded(draw, (570, 300, 930, 540), "AIFood\n饮食记录与营养分析系统", font, fill="#eff6ff")
        rounded(draw, (1160, 190, 1400, 310), "本地数据库", font, fill="#ecfdf5", outline="#059669")
        rounded(draw, (1160, 580, 1400, 700), "识别服务", font, fill="#fff7ed", outline="#ea580c")
        arrow(draw, (280, 400), (570, 400)); arrow(draw, (570, 450), (280, 450))
        arrow(draw, (930, 360), (1160, 260)); arrow(draw, (1160, 300), (930, 420))
        arrow(draw, (930, 500), (1160, 640)); arrow(draw, (1160, 600), (930, 470))
        draw.text((335, 370), "食物、重量、日期、目标", font=cn_font(20), fill="#475569")
        draw.text((335, 455), "统计结果、历史趋势", font=cn_font(20), fill="#475569")
    else:
        nodes = [
            ("选择日期/餐次", 90, 220), ("选择食物并输入重量", 390, 220),
            ("营养换算服务", 720, 220), ("保存餐次记录", 1030, 220),
            ("读取日期范围记录", 390, 560), ("聚合日周月数据", 720, 560), ("图表与页面展示", 1030, 560),
        ]
        for text, x, y in nodes:
            rounded(draw, (x, y, x + 250, y + 110), text, font)
        for a, b in [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6)]:
            ax, ay = nodes[a][1] + 250, nodes[a][2] + 55
            bx, by = nodes[b][1], nodes[b][2] + 55
            if a == 3:
                arrow(draw, (1155, 330), (515, 560))
            else:
                arrow(draw, (ax, ay), (bx, by))
    img.save(path)


def generate_figures():
    FIG_DIR.mkdir(exist_ok=True)
    save_flow(FIG_DIR / "fig3_1_meal_flow.png", "餐次记录业务流程图", ["选择日期", "选择餐次", "搜索食物", "输入重量", "营养换算", "保存记录"])
    save_flow(FIG_DIR / "fig3_2_foodbank_flow.png", "食物库维护业务流程图", ["进入食物库", "搜索/分类浏览", "新增或编辑", "校验营养数据", "保存食物", "用于记录"])
    save_flow(FIG_DIR / "fig3_3_analysis_flow.png", "营养分析业务流程图", ["选择粒度", "确定日期范围", "读取记录", "聚合营养值", "生成图表", "展示结果"])
    save_use_case(FIG_DIR / "fig3_4_top_use_case.png", detail=False)
    save_use_case(FIG_DIR / "fig3_5_detail_use_case.png", detail=True)
    save_er(FIG_DIR / "fig3_6_core_er.png", detail=False)
    save_er(FIG_DIR / "fig3_7_extend_er.png", detail=True)
    save_dfd(FIG_DIR / "fig3_8_top_dfd.png", level2=False)
    save_dfd(FIG_DIR / "fig3_9_detail_dfd.png", level2=True)


def set_font(run, east="宋体", west="Times New Roman", size=12, bold=False):
    run.font.name = west
    run._element.rPr.rFonts.set(qn("w:eastAsia"), east)
    run.font.size = Pt(size)
    run.bold = bold


def set_para(paragraph, first_line=True, align=None):
    pf = paragraph.paragraph_format
    pf.line_spacing = 1.25
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    if first_line:
        pf.first_line_indent = Pt(24)
    if align is not None:
        paragraph.alignment = align


def add_text(doc, text, size=12, bold=False, align=None, first_line=True):
    p = doc.add_paragraph()
    set_para(p, first_line=first_line, align=align)
    r = p.add_run(text)
    set_font(r, size=size, bold=bold)
    return p


def add_heading(doc, text, level=1):
    p = doc.add_paragraph()
    set_para(p, first_line=False, align=WD_ALIGN_PARAGRAPH.LEFT)
    r = p.add_run(text)
    if level == 1:
        set_font(r, east="黑体", size=15, bold=True)
    elif level == 2:
        set_font(r, east="黑体", size=14, bold=True)
    else:
        set_font(r, east="黑体", size=12, bold=True)
    p.style = f"Heading {level}"
    return p


def add_page_break(doc):
    doc.add_page_break()


def add_toc(paragraph):
    run = paragraph.add_run()
    fld = OxmlElement("w:fldSimple")
    fld.set(qn("w:instr"), 'TOC \\o "1-3" \\h \\z \\u')
    run._r.append(fld)


def set_cell_text(cell, text, bold=False):
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_para(p, first_line=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    r = p.add_run(text)
    set_font(r, size=12, bold=bold)


def cover(doc):
    for _ in range(3):
        doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("本科毕业论文")
    set_font(r, east="宋体", size=22, bold=True)
    for _ in range(3):
        doc.add_paragraph()

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("基于 Android 的智能饮食记录与营养分析系统的设计与实现")
    set_font(r, east="黑体", size=18, bold=True)
    for _ in range(3):
        doc.add_paragraph()

    table = doc.add_table(rows=7, cols=2)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    for row in table.rows:
        row.cells[0].width = Cm(4)
        row.cells[1].width = Cm(8)
    rows = [
        ("学院", "计算机科学与技术学院"),
        ("专业", "软件工程"),
        ("学生姓名", "（请填写）"),
        ("学号", "（请填写）"),
        ("指导教师", "（请填写）"),
        ("完成日期", "2026年5月"),
        ("论文题目", "基于 Android 的智能饮食记录与营养分析系统的设计与实现"),
    ]
    for i, (k, v) in enumerate(rows):
        set_cell_text(table.cell(i, 0), k, bold=True)
        set_cell_text(table.cell(i, 1), v)
    add_page_break(doc)


def abstract(doc):
    add_text(doc, "摘  要", size=15, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, first_line=False)
    for t in [
        "随着移动互联网、健康管理理念和智能终端的普及，饮食记录已经从传统的手工笔记逐步转向移动端应用。用户在日常生活中不仅需要记录吃了什么、喝了多少，还希望系统能够对热量、碳水化合物、蛋白质、脂肪及脂肪酸构成等数据进行持续分析，从而辅助形成更健康的饮食习惯。针对个人饮食管理中记录分散、统计困难、历史回溯不直观、食物数据维护效率低等问题，本文设计并实现了一款基于 Android 的智能饮食记录与营养分析系统 AIFood。",
        "系统采用 Java 17 作为主要开发语言，以 Android 原生应用为载体，结合 Room、Repository、ViewModel、LiveData、RecyclerView、ViewPager2、Material Components、MPAndroidChart、CameraX、OkHttp 和 Gson 等技术完成整体实现。系统围绕首页营养分析、餐次记录、饮水打卡、食物库管理、历史视图和食物识别六个核心功能展开，通过本地数据库保存食物、餐次记录和用户目标配置，并通过营养计算服务完成不同时间粒度下的数据聚合与可视化展示。",
        "在系统设计方面，本文按照表现层、业务层、数据访问层和本地存储层进行模块划分。表现层负责页面交互、列表展示、图表展示和日期选择；业务层负责营养换算、目标值计算、日期范围构建和识别结果整理；数据访问层通过 Repository 屏蔽 DAO 细节；本地存储层使用 Room 管理 Food、MealRecord 等实体。系统还通过后台线程执行数据库读写和历史数据统计，降低主线程阻塞风险，提升页面切换与数据加载的稳定性。",
        "测试与运行结果表明，AIFood 能够完成食物库维护、餐次添加、营养指标统计、日周月维度趋势展示、饮水记录管理和食物识别草稿生成等功能。系统界面结构清晰，数据流闭环完整，具备较好的可维护性和扩展能力。后续可在当前架构基础上继续扩展云同步、用户登录、提醒通知、模型识别精度优化和多端协同能力。",
    ]:
        add_text(doc, t)
    add_text(doc, "关键词： Android；饮食记录；营养分析；Room；MPAndroidChart", first_line=False)
    add_page_break(doc)

    add_text(doc, "Abstract", size=15, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, first_line=False)
    for t in [
        "With the rapid development of mobile Internet, personal health management and smart devices, dietary recording is gradually shifting from handwritten notes to mobile applications. Users need not only to record daily food and water intake, but also to obtain continuous analysis of calories, carbohydrate, protein, fat and fatty acid composition. To address problems such as scattered records, difficult statistics, unclear historical review and inefficient food data maintenance, this thesis designs and implements AIFood, an Android-based intelligent dietary recording and nutrition analysis system.",
        "The system is developed with Java 17 on the Android platform. It integrates Room, Repository, ViewModel, LiveData, RecyclerView, ViewPager2, Material Components, MPAndroidChart, CameraX, OkHttp and Gson. AIFood consists of six major modules: nutrition analysis, meal recording, water tracking, food bank management, history view and food recognition. Food data, meal records and user goals are stored locally, while nutrition calculation services aggregate and visualize data at daily, weekly and monthly granularities.",
        "The system adopts a layered architecture including presentation layer, business layer, repository layer and local storage layer. The presentation layer handles interactions, lists, charts and date selection. The business layer processes nutrition conversion, target calculation, date range construction and recognition drafts. The repository layer encapsulates DAO operations, while Room manages core entities such as Food and MealRecord. Database operations and historical statistics are executed in background threads to reduce main-thread blocking and improve stability.",
        "The implementation and verification show that AIFood can support food bank maintenance, meal entry, nutrition statistics, trend visualization, water record management and recognition draft generation. The system has a clear structure, complete data flow and good maintainability. Future work may include cloud synchronization, user authentication, reminder notification, recognition accuracy optimization and cross-device collaboration.",
    ]:
        add_text(doc, t)
    add_text(doc, "Key words: Android; Dietary Recording; Nutrition Analysis; Room; MPAndroidChart", first_line=False)
    add_page_break(doc)


def toc(doc):
    add_text(doc, "目  录", size=15, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, first_line=False)
    p = doc.add_paragraph()
    add_toc(p)
    add_text(doc, "提示：在 Word 中右键目录并选择“更新域”即可生成页码。", first_line=False)
    add_page_break(doc)


chapters = [
("1 绪论", [
("1.1 本课题研究的背景", [
"近年来，居民健康意识明显增强，体重管理、慢病预防、运动健身和营养均衡逐渐成为日常生活中的重要话题。饮食摄入是健康管理中最基础也最容易被忽视的环节。传统饮食记录主要依赖纸质笔记、表格或用户记忆，记录过程不连续，数据难以被复用，也无法形成长期趋势分析。当用户希望了解某一阶段的热量摄入、三大营养素比例或饮水完成情况时，传统方式往往需要人工汇总，效率低且容易出现误差。",
"移动端应用为个人饮食管理提供了更高效的载体。智能手机具备随身携带、交互便捷、摄像头可用和本地存储能力强等特点，适合承担饮食记录、食物查询、营养换算和历史追踪等任务。Android 平台生态成熟，开发工具和组件体系完善，Room、RecyclerView、ViewModel、LiveData 等 Jetpack 组件能够帮助开发者构建结构清晰、可维护性较好的本地应用。对于以个人记录为主的饮食管理系统而言，本地数据库方案可以在不依赖网络的情况下完成核心功能，也有利于保护用户隐私。",
"AIFood 项目正是在这一背景下提出。系统以“记录、计算、分析、回溯”为主线，将饮食记录、饮水打卡、食物库维护、趋势图表和食物识别辅助录入整合在一个 Android 应用中。用户可以按照早餐、午餐、下午加餐、晚餐等餐次记录食物摄入，也可以根据日、周、月三个粒度查看营养变化。系统内置默认食物数据，并支持用户自行新增和编辑食物，为长期使用提供数据基础。",
"从现实需求看，饮食管理并不是单纯记录食物名称，而是需要把用户的日常行为转化为可以理解和比较的数据。用户每天摄入的食物种类、重量和餐次不同，营养价值也存在明显差异。如果系统只能保存文字记录，用户仍然难以判断当天摄入是否合理；如果系统能够将食物重量与单位营养数据结合起来，进一步计算热量、碳水化合物、蛋白质、脂肪以及不同脂肪酸摄入情况，就可以为用户提供更明确的反馈。尤其在减脂、增肌、控糖和改善饮食结构等场景下，连续记录和周期对比比单次记录更有价值。",
"从技术发展看，移动端健康应用正在由简单打卡向数据化、智能化和个性化方向发展。早期应用更多关注记录行为本身，用户录入后只能看到流水账式的历史列表。随着本地数据库、图表组件、摄像头能力和网络识别服务不断成熟，移动应用可以在端侧完成更复杂的计算和展示。AIFood 在本地完成饮食数据持久化和营养聚合，既能保证无网络情况下的基础可用，又能通过食物识别模块为后续智能录入提供扩展空间。",
"从本科毕业设计角度看，饮食记录与营养分析系统具有较好的课题完整性。一方面，系统具有明确的用户场景，需求可以被拆分为首页统计、餐次记录、饮水记录、食物库管理、历史视图和识别辅助录入等模块；另一方面，系统技术链条覆盖 Android 应用开发的多个关键环节，包括界面布局、列表适配、本地数据库、分层架构、异步线程、图表展示、资源管理和接口请求等。通过该课题能够较完整地体现软件工程项目从需求到实现的过程。"]),
("1.2 本课题研究的意义", [
"从用户使用角度看，AIFood 能够降低饮食记录门槛。用户只需选择食物并输入摄入重量，系统即可根据单位营养值自动换算热量、碳水化合物、蛋白质、脂肪及脂肪酸数据，避免用户手动计算。首页通过圆环、进度条和折线图展示摄入情况，使用户能够直观看到目标完成度和剩余空间。",
"从工程实践角度看，本课题具有较强的综合性。系统涉及 Android 页面开发、本地数据库设计、异步线程处理、图表可视化、摄像头调用、HTTP 接口封装、JSON 解析和数据聚合算法等内容。通过完成该系统，可以较完整地训练移动端应用从需求分析、架构设计、编码实现到运行验证的全过程能力。",
"从后续扩展角度看，AIFood 的分层结构为功能演进留下空间。当前系统主要采用本地存储，后续可平滑扩展登录、云同步、AI 食物识别服务、提醒通知和健康建议等功能。Repository 和业务服务的划分也便于在不大幅改动 UI 的情况下替换数据来源。",
"本课题还具有一定的健康教育意义。很多用户对热量和营养素的理解停留在概念层面，难以将其与每日饮食联系起来。系统将常见食物、摄入重量和营养指标关联展示，能够帮助用户逐步建立对食物能量密度和营养构成的直观认识。例如，同样是一次加餐，不同食物在热量和脂肪含量上可能存在较大差异；同样达到热量目标，不同碳水、蛋白质和脂肪比例也会影响饮食结构。通过长期使用，用户可以从数据反馈中调整选择。",
"对移动端开发学习而言，本课题避免了只停留在静态页面展示的不足。AIFood 的核心价值来自真实数据流：默认食物数据被导入本地数据库，用户在餐次页面选择并输入摄入量，系统保存记录并在首页和历史页重新聚合展示。这个过程涉及数据一致性、页面状态同步和异常输入处理，能够检验系统设计是否稳固。相比只实现单个页面或单一 CRUD 功能，本系统更能体现应用级开发的完整性。"]),
("1.3 国内外研究现状", [
"国外健康管理应用起步较早，MyFitnessPal、Cronometer、Lose It! 等产品已形成较成熟的饮食记录和营养分析模式。这类应用通常具备大型食物数据库、条码扫描、运动消耗联动和云端账户同步能力，在数据积累和生态连接方面具有优势。近年来，基于图像识别的食物估计、个性化推荐和可穿戴设备联动也逐渐成为研究热点。",
"国内饮食健康应用主要集中在减脂记录、运动管理、轻断食、体重管理和慢病辅助管理等场景。部分应用能够提供食物热量查询、饮食打卡和报告生成，但在个人自定义食物库、本地离线记录、日周月维度聚合展示以及课程设计级别的可理解架构方面仍存在一定差异。对于本科毕业设计而言，选择一个结构可控、功能完整、可运行验证的 Android 本地系统，更有利于体现软件工程实现能力。",
"综合来看，饮食管理系统已经具备明确的现实需求和成熟的技术基础，但不同项目在数据可靠性、交互效率、可扩展架构和识别辅助录入方面仍有改进空间。AIFood 以本地可用、结构清晰、营养统计完整为设计重点，在移动端完成核心闭环。",
"从国外应用的发展路径看，健康管理产品通常依赖较大规模的标准化食物数据库和用户社区数据。用户可以通过搜索、扫描条形码或调用外部设备数据快速完成记录，系统再生成日报、周报或趋势分析。这类产品的优势是数据来源丰富、平台服务完整，但也存在一定问题：部分高级功能依赖付费订阅，食物数据本地化程度受地区影响较大，中文饮食场景下的菜品识别和营养估计并不总是准确。此外，云端账户体系虽然便于跨设备同步，但也增加了隐私保护和网络依赖问题。",
"国内相关应用在本地饮食习惯适配方面更有优势，例如对家常菜、中式主食、饮品和地方食品的覆盖相对更贴近用户。但不少应用更强调体重管理或商业运营，系统内部实现对于学习者并不可见。对于本科毕业设计来说，直接复刻成熟商业应用并不现实，也没有必要。更合理的做法是聚焦核心业务闭环，在可控范围内实现食物库、记录、统计和历史回溯，并在架构上保留扩展能力。",
"在学术和工程实践中，移动健康系统常见研究方向包括数据采集、用户行为分析、可视化反馈、智能推荐和隐私保护等。饮食记录系统处于这些方向的交叉位置：它既需要方便用户输入，又需要对输入数据进行结构化处理；既要展示短期结果，也要保留长期分析可能；既要追求功能丰富，也要控制移动端性能和交互复杂度。AIFood 的设计选择是先保障本地记录和营养分析的稳定性，再通过食物识别等模块逐步增强智能化能力。"]),
("1.4 本课题所做的主要工作", [
"本文围绕 AIFood 的设计与实现展开，主要工作包括：第一，分析饮食记录与营养统计的业务需求，确定首页营养分析、餐次记录、饮水打卡、食物库、历史视图和食物识别模块；第二，设计系统总体架构和核心数据模型，明确 Food、MealRecord、用户目标和日期范围等关键对象；第三，基于 Room 和 Repository 实现本地数据持久化与访问封装；第四，基于 NutritionService 和 NutritionCalculator 实现营养数据换算与聚合；第五，完成多页面 Android UI、图表展示和后台线程优化；第六，对系统运行效果进行验证并总结不足。",
"在需求分析阶段，本文将用户的使用流程拆分为多个连续动作：查看当天状态、添加餐次记录、维护食物数据、记录饮水、切换日期和查看历史趋势。每个动作都对应系统中的具体页面和数据处理逻辑。通过这种方式，论文不是孤立描述功能点，而是从用户实际使用链路出发说明系统为什么需要这些模块。",
"在系统设计阶段，本文采用分层架构组织代码。表现层负责页面展示和用户交互，业务层负责营养换算、目标计算和日期范围处理，数据访问层通过 Repository 统一封装数据库操作，本地存储层使用 Room 管理实体和 DAO。该设计使各模块职责清晰，也便于在论文中说明系统调用关系和数据流向。",
"在实现阶段，本文结合 AIFood 项目已有代码和运行截图，对首页统计、餐次记录、食物库、历史视图、饮水记录和食物识别进行说明。系统通过后台线程处理数据库读写和较大范围历史数据构建，避免影响主线程响应；通过 MPAndroidChart 和自定义 View 展示营养趋势和目标完成情况；通过默认食物数据导入和用户自定义食物维护提高记录效率。"]),
("1.5 本章小结", [
"本章介绍了课题背景、研究意义、国内外研究现状以及本文主要工作。饮食记录与营养分析具有明确的现实需求，Android 本地应用能够较好地承载该场景。通过对现有应用和技术条件的分析可以看出，一个面向个人用户的饮食记录系统既要关注功能完整性，也要重视数据结构、页面交互和长期维护能力。AIFood 选择以本地数据库为基础，以营养计算和多维度展示为核心，以食物识别作为扩展方向，符合本科毕业设计对可实现性和工程完整性的要求。",
"本章同时明确了后续论述的重点：论文并不把系统描述为单一页面或简单记账工具，而是围绕饮食数据的采集、换算、保存、聚合和反馈展开。只有把这些环节串联起来，系统才能真正帮助用户理解饮食行为，并体现移动端软件设计的完整过程。",
"后续章节将从系统开发工具与技术、需求分析、总体设计、详细实现、测试结果和总结展望等方面展开说明。其中第二章介绍 Android、Room、Repository、图表和识别相关技术；第三章分析系统功能与非功能需求；第四章说明架构、模块和数据流设计；第五章结合代码结构说明主要功能实现；第六章对系统运行结果进行测试分析；第七章总结系统成果并提出改进方向。"])]),
("2 系统开发工具与技术", [
("2.1 系统开发环境", [
"AIFood 使用 Android Studio 作为主要开发工具，项目采用 Gradle Kotlin DSL 管理构建配置。系统 compileSdk 为 35，minSdk 为 24，targetSdk 为 35，开发语言为 Java 17。该配置能够覆盖较新的 Android 平台能力，同时兼顾一定范围的设备兼容性。",
"版本控制采用 Git，项目目录遵循 Android 标准结构，主代码位于 app/src/main/java/com/example/food，资源文件位于 app/src/main/res。运行截图、默认食物数据和布局资源分别存放在独立目录中，便于论文整理和功能验证。",
"Android Studio 集成了代码编辑、资源预览、模拟器调试、Gradle 构建和日志查看等功能，适合完成 Android 应用从开发到运行验证的全过程。AIFood 在开发过程中需要频繁调整 XML 布局、Java 逻辑和资源文件，借助 Android Studio 的布局预览、自动补全和错误提示可以提升开发效率。Gradle Kotlin DSL 则用于统一声明 SDK 版本、依赖库、BuildConfig 字段和编译选项，使项目配置更加集中。",
"项目使用 Java 17 作为源码兼容版本，但构建时需要注意 JDK 与 Gradle、Kotlin DSL 的兼容关系。在实际验证中，默认命令行 Java 版本过高会导致 Gradle 解析失败，因此采用 JDK 17 构建更稳妥。该情况也说明开发环境不仅影响编码过程，还会直接影响项目能否被稳定复现。"]),
("2.2 Android 与 Java 技术", [
"Android 原生开发能够直接调用系统组件和硬件能力，适合实现摄像头、页面导航、本地数据库、文件资源读取和通知等功能。Java 作为 Android 传统主力语言，语法稳定、生态成熟，适合本科阶段进行工程实现与代码说明。",
"AIFood 的页面由 Activity、Fragment、自定义 View 和 Adapter 组成。MainActivity 承担底部导航和主页面容器职责，HomeFragment、MealFragment、FoodBankFragment 等页面负责不同业务功能。系统通过 RecyclerView 展示食物列表、餐次记录和历史日期项，通过自定义 View 绘制热量圆环、脂肪构成环和营养分布效果。",
"在 Android 应用中，Activity 通常负责承载独立页面或页面容器，Fragment 更适合承载可切换的业务区域。AIFood 采用底部导航组织首页、餐次、饮水和食物库等主要入口，这种方式符合移动端常见交互习惯，也便于用户在不同功能之间快速切换。RecyclerView 用于处理可变长度列表，相比传统 ListView 具有更好的复用机制，适合展示食物库、餐次记录和日期单元。",
"Java 代码在项目中承担业务逻辑、数据模型和页面控制职责。项目通过实体类描述食物和餐次记录，通过工具类处理日期格式和显示计算，通过服务类处理营养换算。与把所有逻辑写在页面中相比，这种组织方式更便于阅读和维护，也使论文能够清晰说明每一层代码的作用。"]),
("2.3 Room 数据库技术", [
"Room 是 Android Jetpack 提供的 SQLite 抽象层，能够通过实体类、DAO 接口和数据库类降低原生 SQLite 的使用复杂度。AIFood 使用 Room 管理 Food 与 MealRecord 两类核心实体，并通过 DateTypeConverter 处理 Date 类型转换。AppDatabase 采用单例模式创建数据库实例，数据库名称为 food_app_database，版本号为 3。",
"Room 的优势在于编译期校验 SQL、实体映射清晰、与 LiveData 等组件配合方便。对于 AIFood 这类以本地记录为主的系统，Room 可以提供稳定的数据持久化能力，并支持后续数据库迁移。",
"AIFood 的数据具有明显的结构化特征。食物数据包含名称、分类、单位重量和营养成分，餐次记录包含日期、餐次类型、摄入量和换算后的营养值。如果使用普通文件保存，后续查询、筛选和按日期聚合都会比较困难。Room 基于 SQLite 提供查询能力，既保留了本地存储的轻量特点，又能满足按日期范围读取、按食物名称搜索和记录增删改查等需求。"]),
("2.4 Repository 与异步线程", [
"Repository 模式用于隔离 UI 层和数据访问层。AIFood 中的 FoodRepository、MealRepository 和 HistoryRepository 负责封装 DAO 操作，页面不直接处理 SQL 或数据库实例。这样可以降低模块耦合，并使业务逻辑更容易维护。",
"数据库读写如果直接运行在主线程，可能造成界面卡顿。项目通过 AppExecutors 将 IO 操作放入后台线程执行，在数据加载完成后再回调更新页面。历史页日、周、月数据构建范围较大，更需要后台处理以保证滑动和切换体验。",
"Repository 的另一个作用是为后续扩展预留接口。如果未来系统接入云端同步或远程食物数据库，UI 层理论上不需要直接改为访问网络接口，而是由 Repository 内部决定数据来源。对于毕业设计而言，这种写法比页面直接调用 DAO 更符合长期维护思路，也能让系统结构与实际软件开发习惯保持一致。",
"异步线程处理还与用户体验直接相关。饮食记录系统虽然单条数据不大，但历史页可能一次处理一个月甚至更长时间范围内的记录。如果这些统计全部放在主线程，页面切换时就可能出现停顿。AIFood 将数据库访问和部分数据构建放到后台执行，再将结果回传给页面渲染，能够降低主线程压力。"]),
("2.5 图表与识别相关技术", [
"营养趋势展示使用 MPAndroidChart。该库能够绘制折线图、柱状图等常见统计图，适合表现日、周、月维度下的热量和营养素变化。AIFood 结合 PeriodChartMarkerView 提供图表选中提示，使用户能够查看具体日期数据。",
"食物识别模块使用 CameraX 调用摄像头，使用 OkHttp 封装网络请求，使用 Gson 解析识别结果。BuildConfig 中通过 local.properties 注入识别服务地址、密钥和模型名称，避免将敏感配置硬编码在源码中。",
"图表展示能够把抽象数字转化为更容易理解的趋势信息。用户如果只看到某一天摄入了多少千卡，难以判断饮食是否稳定；如果能看到一周或一个月的变化曲线，就可以发现摄入过高或过低的日期。MPAndroidChart 在 AIFood 中承担了这种数据表达职责，使首页不仅是记录结果的展示页，也是用户理解饮食状态的分析页。",
"食物识别相关技术主要用于降低录入成本。用户手动搜索食物和输入重量仍然是可靠方式，但在实际使用中，拍照识别可以作为辅助入口。CameraX 简化了 Android 摄像头调用流程，OkHttp 负责稳定发送请求，Gson 将服务返回的 JSON 转换为 Java 对象。系统采用识别草稿而不是直接保存记录，是为了避免识别误差直接污染正式数据。"]),
("2.6 本章小结", [
"本章介绍了系统开发环境和关键技术。Android 原生开发提供了完整的移动端能力，Room 负责本地持久化，Repository 保持结构清晰，MPAndroidChart 支撑统计展示，CameraX 与 OkHttp 为食物识别扩展提供基础。",
"总体来看，AIFood 选择的技术栈以稳定、可实现和易维护为主要原则。系统没有引入过重的后端架构，而是在 Android 端完成核心业务闭环；没有把计算逻辑散落在页面中，而是通过服务类、仓库层和数据库层进行组织。这样的技术路线符合个人饮食记录应用的规模，也适合本科毕业设计进行说明和验证。"])]),
("3 系统需求分析", [
("3.1 可行性分析", [
"3.1.1 技术可行性分析",
"AIFood 的核心功能均可以由 Android 平台和现有开源组件完成，技术路线具有可实施性。系统使用 Java 17 进行 Android 原生开发，页面部分可由 Activity、Fragment、RecyclerView、ViewPager2 和自定义 View 实现；数据持久化可由 Room 完成，Food、MealRecord 等结构化实体可以映射为本地数据库表；营养分析部分可通过 Java 业务类完成计算，再借助 MPAndroidChart 展示趋势图。对于食物识别扩展，CameraX 可以处理拍照和预览，OkHttp 可以封装接口请求，Gson 可以解析识别服务返回的 JSON 数据。项目当前已经具备可编译运行的 Android 工程结构，相关依赖在 Gradle 中集中管理，整体技术组合成熟稳定。需要注意的是，构建环境应使用与项目兼容的 JDK 17，避免过高版本 Java 导致 Gradle Kotlin DSL 解析异常。综合来看，系统不存在明显技术瓶颈，主要风险集中在识别服务准确率和外部接口稳定性，核心本地功能不依赖网络即可运行。",
"3.1.2 操作可行性分析",
"AIFood 面向普通个人用户，操作流程应尽量接近日常生活习惯。用户打开应用后可以先在首页查看当天热量和营养目标完成情况，再进入餐次页面按早餐、午餐、下午加餐、晚餐等类别添加食物。添加记录时，用户只需要选择食物并输入摄入重量，系统自动完成营养换算，不要求用户理解复杂的数据库或营养计算过程。食物库页面提供搜索、分类浏览、新增和编辑能力，适合用户维护个人常吃食物；饮水页面提供快捷饮水量和自定义输入，降低重复操作成本；历史页面通过日、周、月视图帮助用户回顾过去记录。整体交互以点击、选择、输入和查看为主，符合移动端应用常规操作方式。对于首次使用的用户，默认食物数据可以降低空库带来的学习成本。即使用户不使用食物识别功能，也能通过手动搜索和添加完成核心记录，因此系统具备较好的操作可行性。",
"3.1.3 经济可行性分析",
"从开发成本看，AIFood 使用 Android Studio、Gradle、Java、Room、RecyclerView、MPAndroidChart、CameraX、OkHttp 和 Gson 等常见工具与依赖，均可在学习和毕业设计场景下免费使用，不需要购买商业开发平台。系统以本地数据库为核心，不强制依赖服务器、云数据库或付费接口，因此基础版本的开发、测试和运行成本较低。用户侧只需要一台支持 Android 系统的手机或模拟器即可运行，不需要额外硬件设备。食物识别功能如果接入外部模型服务，可能产生网络服务费用或接口调用成本，但该功能在系统中属于辅助录入，不影响饮食记录、食物库、饮水记录、历史回溯和首页分析等核心功能使用。对于本科毕业设计而言，项目成本主要体现为开发时间和测试时间，而非资金投入。系统后续若扩展云同步、账户体系或高精度识别服务，再根据实际用户规模选择服务器和接口方案即可。因此该课题在经济上可控，适合作为毕业设计实现。"]),
("3.2 系统总体需求分析", [
"3.2.1 适用对象需求分析",
"AIFood 的主要适用对象是具有日常饮食记录和健康管理需求的个人用户，包括关注体重管理的学生和上班族、需要控制饮食结构的健身人群、希望改善饮水习惯的普通用户，以及希望了解热量和营养素摄入情况的健康管理初学者。这类用户的共同特点是需要一个随手可用、操作简单、反馈直观的移动端工具，而不是复杂的专业营养分析平台。用户在实际生活中记录饮食时，往往处于饭前、饭后或外出场景，操作时间有限，因此系统必须减少录入步骤，避免让用户在多个页面之间反复跳转。",
"从用户能力看，目标用户不一定熟悉营养学概念。系统需要把热量、碳水化合物、蛋白质、脂肪和脂肪酸构成等指标转化为易理解的页面反馈。例如首页通过进度、圆环和趋势图表现目标完成情况，历史页面通过日、周、月视图帮助用户理解长期变化，食物库通过分类和搜索降低查找成本。用户真正关心的不是数据库如何存储，而是能否快速知道今天吃了多少、还剩多少、最近是否摄入偏高。因此系统需求应围绕记录效率和反馈清晰度展开。",
"从使用频率看，饮食记录属于高频但单次操作较短的行为。用户可能每天多次添加餐次记录，也可能在晚上统一补录。因此系统需要支持指定日期切换，避免只能记录当天数据。对于经常吃固定食物的用户，食物库维护和默认食物导入非常重要；对于偶尔尝试新菜品的用户，新增食物和识别草稿可以提高灵活性。饮水记录虽然数据结构较简单，但和饮食管理共同构成健康行为记录，因此也属于目标用户的实际需求。综合来看，AIFood 的适用对象并非专业营养师，而是需要个人饮食管理辅助的普通移动端用户。",
"3.2.2 系统功能需求分析",
"根据适用对象特点，AIFood 的功能需求可以概括为记录、维护、分析、回溯和辅助录入五类。记录需求主要包括餐次记录和饮水记录。餐次记录要求用户能够选择日期、选择餐次、选择食物、输入摄入重量，并由系统自动换算营养值；饮水记录要求支持快捷饮水量、自定义输入、目标设置和记录删除。维护需求主要体现在食物库，系统需要内置默认食物数据，同时允许用户新增、编辑、删除和搜索食物，保证记录数据来源可持续扩展。",
"分析需求主要体现在首页营养分析。系统需要根据用户选择的日期和粒度展示日、周、月三个维度的数据。日视图关注当天总热量、碳水化合物、蛋白质、脂肪及脂肪构成；周视图和月视图关注趋势变化，帮助用户发现连续多天摄入偏高或偏低的情况。回溯需求主要体现在历史页面，系统需要支持用户按日、周、月浏览记录，并与首页和餐次页面的日期状态联动，使用户可以从历史入口跳转到具体日期查看。",
"辅助录入需求主要体现在食物识别模块。用户通过拍照或选择图片提交给识别服务后，系统生成候选食物和识别草稿。由于识别存在不确定性，系统不能直接把识别结果写入正式记录，而应让用户确认名称、重量和营养信息后再保存。除功能需求外，系统还应满足基本非功能需求：数据库操作不能阻塞主线程，空数据和非法输入应被妥善处理，页面布局应保持清晰，核心功能在离线状态下仍可使用。"]),
("3.3 主要业务分析", [
"AIFood 的主要业务围绕用户饮食数据从录入到反馈的完整链路展开。系统并不是简单保存几条文本记录，而是将用户输入的日期、餐次、食物和重量转化为可统计的营养数据，再通过首页和历史页形成反馈。参考常见管理系统论文的业务分析写法，本节将系统拆分为餐次记录业务、食物库维护业务、营养分析业务、饮水记录业务和食物识别业务，并分别说明流程和关键约束。",
"餐次记录业务是系统最核心的业务。用户进入餐次页面后，首先选择要记录的日期，再选择早餐、午餐、下午加餐或晚餐等餐次。随后用户从食物库中搜索或浏览食物，输入实际摄入重量，系统调用营养计算服务按照食物单位营养值进行比例换算。换算结果包括热量、碳水化合物、蛋白质、脂肪以及脂肪酸等数据。用户确认后，系统将记录保存为 MealRecord。保存记录时应保留营养快照，这样即使后续食物库数据被修改，历史记录仍能保持原始统计结果。餐次记录业务流程如图 3-1 所示。",
"[[FIG:thesis_figures/fig3_1_meal_flow.png:图3-1 餐次记录业务流程图]]",
"食物库维护业务为餐次记录提供基础数据。系统首次运行时通过默认食物数据导入器导入常见食物，用户也可以根据个人饮食习惯新增食物。新增或编辑食物时，需要填写食物名称、分类、单位数量、单位名称、热量、碳水化合物、蛋白质、脂肪和脂肪酸构成等字段。系统应对名称为空、单位重量无效和营养数值异常等情况进行限制，避免错误数据影响后续统计。食物库还需要提供搜索和分类展示能力，使用户在大量食物中快速定位目标。食物库维护业务流程如图 3-2 所示。",
"[[FIG:thesis_figures/fig3_2_foodbank_flow.png:图3-2 食物库维护业务流程图]]",
"营养分析业务负责把餐次记录转化为可读的统计结果。用户在首页选择日、周或月粒度后，系统先根据选中日期计算查询范围，再通过 Repository 从本地数据库读取 MealRecord 列表。日视图下，系统聚合当天所有记录，展示总热量、目标对比、剩余热量和营养素构成；周视图和月视图下，系统按自然日生成连续数据点，并将未来日期置零，避免图表产生误导。图表展示不仅能显示数值，还能帮助用户发现一段时间内摄入趋势是否稳定。营养分析业务流程如图 3-3 所示。",
"[[FIG:thesis_figures/fig3_3_analysis_flow.png:图3-3 营养分析业务流程图]]",
"饮水记录业务相对独立，但与健康管理目标密切相关。用户进入饮水页面后，可以选择指定日期，使用快捷按钮添加常见饮水量，也可以手动输入自定义饮水量。系统根据当日累计饮水量和用户设置的每日目标展示完成进度。饮水记录需要支持删除，避免用户误操作后无法修正。由于饮水数据不涉及复杂营养换算，业务重点在于快速记录、目标反馈和日期回溯。",
"食物识别业务用于辅助用户录入。用户拍摄或选择食物图片后，系统通过识别客户端调用外部服务，服务返回候选食物名称和每 100 克营养估计。系统将结果组织为识别草稿，用户确认后才进入正式记录流程。该设计体现了数据可靠性原则：识别结果可以提高效率，但不能完全替代用户判断。若识别失败或网络不可用，系统应允许用户继续使用手动添加方式完成记录。"]),
("3.4 系统用例分析", [
"3.4.1 顶层用例图",
"AIFood 的使用者主要为普通用户。用户进入系统后，可以查看首页营养分析、管理餐次记录、维护食物库、记录饮水、查看历史数据和使用食物识别辅助录入。系统没有单独设置管理员角色，原因是当前版本定位为个人本地应用，食物库维护和目标设置均由用户本人完成。顶层用例图如图 3-4 所示。",
"[[FIG:thesis_figures/fig3_4_top_use_case.png:图3-4 AIFood 顶层用例图]]",
"3.4.2 细化用例",
"从细化用例看，查看营养分析可以进一步分为选择日周月粒度、查看目标完成情况和查看趋势图；管理餐次记录可以分为新增记录、编辑记录、删除记录和按日期查看记录；维护食物库可以分为搜索食物、分类浏览、新增食物、编辑食物和删除食物；饮水记录可以分为快捷添加、自定义添加、设置目标和删除记录；食物识别可以分为拍照、提交识别、查看候选和确认草稿。细化用例图如图 3-5 所示。",
"[[FIG:thesis_figures/fig3_5_detail_use_case.png:图3-5 AIFood 细化用例图]]"]),
("3.5 系统E-R图分析", [
"AIFood 的核心数据实体包括 Food、MealRecord 和 UserGoal。Food 表示食物基础数据，保存名称、分类、单位和营养成分；MealRecord 表示用户某一日期、某一餐次的摄入记录，保存食物名称、摄入量和营养快照；UserGoal 表示用户设置的营养目标，用于首页目标对比。Food 与 MealRecord 之间存在被选择生成记录的关系，一个 Food 可以被多条 MealRecord 引用；MealRecord 与 UserGoal 之间不直接从属，但在统计展示时共同参与目标对比。核心 E-R 图如图 3-6 所示。",
"[[FIG:thesis_figures/fig3_6_core_er.png:图3-6 AIFood 核心实体 E-R 图]]",
"除核心实体外，系统还存在识别草稿、识别候选和饮水记录等扩展数据对象。RecognitionDraft 用于暂存识别结果，FoodCandidate 表示识别服务返回的候选食物，WaterRecord 表示用户某日饮水量及目标完成情况。当前项目中部分对象以业务模型或偏好配置方式存在，但从需求分析角度仍可作为后续数据库扩展实体。扩展 E-R 图如图 3-7 所示。",
"[[FIG:thesis_figures/fig3_7_extend_er.png:图3-7 AIFood 扩展实体 E-R 图]]"]),
("3.6 系统数据流图", [
"系统数据流体现用户输入、系统处理和数据存储之间的关系。顶层数据流中，用户向 AIFood 输入食物、重量、日期、饮水量和目标值，系统将结构化数据写入本地数据库，并在需要时调用识别服务获取候选结果。系统再将统计结果、历史趋势和识别草稿反馈给用户。顶层数据流图如图 3-8 所示。",
"[[FIG:thesis_figures/fig3_8_top_dfd.png:图3-8 AIFood 顶层数据流图]]",
"进一步细化记录与分析流程时，用户先选择日期和餐次，再选择食物并输入摄入重量。系统调用营养换算服务生成营养数据，将餐次记录保存到数据库。首页或历史页加载时，系统按日期范围读取记录，聚合为日、周、月统计结果，最终由图表和页面组件展示。该数据流保证了录入数据能够被后续分析复用，也说明 Repository、业务服务和 UI 状态之间的联系。记录与分析数据流图如图 3-9 所示。",
"[[FIG:thesis_figures/fig3_9_detail_dfd.png:图3-9 AIFood 记录与分析数据流图]]"]),
("3.7 本章小结", [
"本章从可行性、总体需求、主要业务、用例、E-R 图和数据流图六个方面对 AIFood 进行了需求分析。通过分析可以看出，系统在技术、操作和经济方面均具备实现条件；目标用户主要是有个人饮食记录和健康管理需求的普通移动端用户；核心业务围绕食物库、餐次记录、营养分析、饮水记录、历史回溯和识别辅助录入展开。上述分析为后续系统总体设计和详细实现提供了依据。"])]),
("4 系统总体设计", [
("4.1 总体架构设计", [
"AIFood 采用分层架构设计，自上而下包括表现层、业务层、数据访问层和本地存储层。表现层由 Activity、Fragment、Adapter 和自定义 View 构成，负责用户交互和界面渲染。业务层由 NutritionService、NutritionCalculator、日期工具和识别结果模型组成，负责数据换算与状态组装。数据访问层由 Repository 组成，对 DAO 操作进行统一封装。本地存储层由 Room 数据库、实体类、DAO 和默认食物导入器组成。",
"这种架构的优点是职责边界清晰。页面不直接依赖数据库实现，数据库调整时主要影响 Repository 和 DAO；营养计算逻辑集中在业务服务中，避免在多个页面重复实现；日期范围构建和图表数据构建由 ViewModel 完成，便于首页和历史页复用思路。"]),
("4.2 功能模块设计", [
"首页模块负责展示核心统计结果，是系统的主入口。HomeViewModel 根据用户选择日期和统计粒度计算可见日期范围，再从 MealRepository 获取记录，最后聚合成 HomeUiState 供页面渲染。日视图侧重当日摄入与目标对比，周月视图侧重趋势分析。",
"餐次模块负责饮食记录闭环。AddActivity 用于选择食物并输入重量，MealFragment 和 MealShowFragment 用于展示指定日期下的餐次记录。MealAdapter 和 FoodAdapter 负责列表适配，CalendarAdapter 负责日期选择。",
"食物库模块负责维护基础数据。FoodBankFragment 展示分类后的食物列表，AddFoodActivity 支持新增和编辑。FoodSeedImporter 在首次使用时导入 default_foods.json，保证系统初始可用。",
"历史模块用于回溯数据。HistoryActivity 和 HistoryViewModel 根据不同视图构造日期网格，WeekBandOverlayView 用于周视图视觉标识。历史模块与 SelectedDateViewModel 配合，使用户在不同页面选择的日期保持一致。",
"食物识别模块由 FoodRecognitionActivity、FoodRecognitionClient、HttpFoodRecognitionClient、FoodCandidate、RecognitionResult 等类组成。该模块并不直接替代用户判断，而是生成识别候选和草稿，用户确认后再进入记录流程。"]),
("4.3 数据库设计", [
"系统核心数据库包含 Food 和 MealRecord 两类实体。Food 表用于保存食物基础信息，包括名称、分类、单位数量、单位名称、热量、碳水化合物、蛋白质、脂肪、饱和脂肪酸、单不饱和脂肪酸和多不饱和脂肪酸等字段。MealRecord 表用于保存用户饮食记录，包括食物名称、餐次类型、日期、摄入重量和本次换算后的营养值。",
"将记录中的营养值保存为快照，有利于保证历史记录稳定。即使用户后续修改食物库中某个食物的营养参数，已保存的历史记录仍能保持当时的计算结果，避免历史数据被意外改变。"]),
("4.4 数据流设计", [
"以添加餐次记录为例，用户在页面选择食物并输入摄入量后，系统先通过 NutritionService.calculateByAmount 按食物单位值计算营养数据，再由 MealRepository 将 MealRecord 写入 Room 数据库。首页或历史页刷新时，通过 Repository 按日期范围读取记录，再在 ViewModel 中聚合成页面所需的 UI 状态。",
"以首页周视图为例，HomeViewModel 先根据选中日期计算周一到下周一的半开区间，然后查询该区间内的记录。系统按自然日聚合热量和营养素，并补齐没有记录的日期点，对未来日期置零，最终生成 PeriodSeries 供图表展示。"]),
("4.5 本章小结", [
"本章完成了系统架构、功能模块、数据库和数据流设计。AIFood 的设计重点是分层清楚、数据闭环完整和后续可扩展。"])]),
("5 系统详细实现", [
("5.1 本地数据库实现", [
"AppDatabase 使用 @Database 注解声明 Food 和 MealRecord 两个实体，数据库版本为 3，并通过 @TypeConverters 引入 DateTypeConverter。数据库实例采用 volatile 与 synchronized 结合的双重检查单例方式创建，避免重复构建数据库对象。",
"数据库迁移方面，项目保留了 MIGRATION_1_2 和 MIGRATION_2_3。第一段迁移保持结构兼容，食物单位归一由 FoodSeedImporter 处理；第二段迁移删除旧的 water_records 表，说明项目在迭代过程中对数据结构进行了整理。虽然当前饮水功能的具体持久化方式与餐次记录分离，但数据库迁移策略体现了对版本兼容的考虑。"]),
("5.2 食物库与默认数据导入实现", [
"FoodSeedImporter 用于导入 app/src/main/res/raw/default_foods.json 中的默认食物数据。首次进入系统时，用户无需手动录入所有基础食物，即可直接选择常见食物进行记录。食物库页面支持分类展示和搜索，减少用户在大量食物中查找的成本。",
"新增和编辑食物时，系统需要校验名称、单位和营养数值。食物营养字段按单位数量保存，例如每 100 克的热量和营养素。这样在后续记录实际摄入量时，只需按比例换算即可。"]),
("5.3 餐次记录实现", [
"餐次记录模块将用户一天的饮食拆分为不同餐次，符合真实生活中的记录习惯。用户选择食物后输入重量，系统通过 NutritionService 计算营养值。该服务先判断食物对象和摄入量是否合法，再根据 unitAmount 计算比例，最后生成 NutritionData。",
"MealRecord 保存本次摄入的具体营养数据，列表页面按日期和餐次展示记录。通过 Repository 封装插入、查询、删除等操作后，UI 层不需要了解 Room 的细节。这样的实现方式降低了页面复杂度，也便于后续增加批量导入或云端同步。"]),
("5.4 首页营养分析实现", [
"首页是系统最重要的分析页面。HomeViewModel 中定义了 DAY、WEEK、MONTH 三种 TimeGranularity。用户选择不同粒度后，ViewModel 调用 calculateVisibleRange 计算可见范围：日视图为当天零点到次日零点，周视图以周一为起始，月视图为当月第一天到下月第一天。",
"系统在日视图中聚合当天所有 MealRecord，得到总热量和营养素数据，并计算脂肪构成比例。若饱和脂肪酸为零，则比例置零，避免除零错误。周视图和月视图中，系统构建 PeriodSeries，将每天的热量、碳水化合物、蛋白质和脂肪汇总为 DailyAggregatePoint。对于未来日期，系统将实际摄入置零，避免图表误导用户。",
"用户目标值保存在 UserGoalPreferences 中。HomeViewModel 根据碳水化合物、蛋白质和脂肪目标，分别乘以 4、4、9 千卡每克的能量系数，计算总热量目标。该方法符合常见营养学换算规则，并使热量目标与宏量营养素目标保持一致。"]),
("5.5 历史视图实现", [
"历史视图提供日、周、月三类浏览方式。日视图适合查看某一天是否有记录，周视图适合观察一周内的连续情况，月视图适合更大范围的回溯。HistoryViewModel 负责构造不同维度的数据结构，页面只负责展示和响应点击。",
"由于历史页可能一次性构造较多日期单元，项目将相关计算放入后台线程，避免切换月份或年份时阻塞界面。WeekBandOverlayView 用于在周视图中绘制连续范围标识，使用户更容易识别当前选择区间。"]),
("5.6 饮水记录实现", [
"饮水记录模块补充了饮食记录之外的健康管理需求。用户可以使用快捷按钮添加常见饮水量，也可以输入自定义数值。页面展示每日目标和当前完成进度，帮助用户及时了解饮水情况。指定日期切换后，用户可以查看历史饮水记录并进行删除。",
"饮水模块与饮食模块在业务含义上不同，因此在界面上独立呈现，但在日期选择和健康目标思路上保持一致。这样的设计既避免了首页数据过载，又保证了功能入口清晰。"]),
("5.7 食物识别实现", [
"食物识别模块通过 FoodRecognitionActivity 调用摄像头或图片能力，使用 HttpFoodRecognitionClient 将图片信息提交给识别服务。识别结果被解析为 RecognitionResult，其中包含 FoodCandidate、NutritionPer100g 和 RecognitionDraft 等对象。",
"该模块采用候选草稿方式而不是直接写入正式记录，原因在于食物图像识别存在不确定性。用户需要确认食物名称、重量和营养信息后再保存，这样可以降低错误识别对历史数据的影响。BuildConfig 从 local.properties 注入 API 配置，也减少了敏感信息泄露风险。"]),
("5.8 界面实现", [
"系统界面围绕底部导航组织：首页、餐次、饮水、食物库和历史页面各自承担明确任务。布局文件集中在 res/layout 目录，图标和背景资源集中在 drawable 目录，颜色和尺寸资源集中在 values 目录。统一资源管理有利于保持界面风格一致。",
"首页使用自定义 View 和图表展示营养数据，餐次页使用列表展示不同餐次，食物库使用分类列表提升查找效率，历史页使用日期网格和视图切换帮助用户回溯数据。运行截图表明，系统已形成较完整的移动端交互流程。"]),
("5.9 本章小结", [
"本章对数据库、食物库、餐次记录、首页分析、历史视图、饮水记录、食物识别和界面实现进行了说明。系统实现与需求分析基本对应，核心数据流能够闭环运行。"])]),
("6 系统测试与结果分析", [
("6.1 测试环境", [
"系统测试在 Android Studio 和 Android 模拟器或真机环境中进行，项目使用 Gradle 构建。测试重点包括应用启动、页面导航、食物库导入、餐次添加、营养计算、历史视图切换、饮水记录和识别配置读取等功能。"]),
("6.2 功能测试", [
"首页测试主要检查日、周、月视图切换是否正常，热量目标、剩余热量和营养素统计是否随餐次记录变化。测试结果表明，当指定日期存在多条餐次记录时，首页能够正确聚合并展示；当日期无记录时，页面能展示空状态或零值，不影响应用运行。",
"餐次记录测试包括从食物库选择食物、输入不同重量、保存记录、查看指定日期记录和删除记录。测试结果表明，营养值会随重量按比例变化，删除后首页统计同步减少。",
"食物库测试包括默认数据加载、搜索、分类浏览、新增食物、编辑食物和删除食物。测试结果表明，食物库能够为餐次记录提供基础数据，用户自定义食物也能参与营养换算。",
"历史视图测试包括日视图、周视图和月视图切换。测试结果表明，系统能够显示不同时间范围下的记录情况，日期选择可以与其他页面联动。",
"饮水记录测试包括快捷添加、自定义输入、目标设置、日期切换和删除。测试结果表明，饮水进度能够随记录变化更新，历史日期数据可查看。"]),
("6.3 性能与稳定性测试", [
"性能测试重点观察页面切换和历史范围构建。由于数据库读写和较大范围统计放在后台线程执行，系统在常规数据量下未出现明显卡顿。RecyclerView 与 Adapter 的使用也减少了列表渲染开销。",
"稳定性测试重点检查空数据、无效输入和未来日期。首页周月视图对未来日期置零，避免将未发生的日期显示为异常数据；营养计算服务对空食物和非正数摄入量返回空数据，降低异常风险。"]),
("6.4 运行结果展示", [
"项目目录中保存了首页日视图、首页周视图、首页月视图、饮食记录页面、添加页、食物库页面、饮水记录页面、历史页日视图、历史页周视图和历史页月视图等运行截图。这些截图表明系统已覆盖主要功能入口，页面之间能够形成完整操作链路。",
"从运行结果看，AIFood 的首页统计信息较集中，适合作为用户每日查看入口；餐次页面符合记录习惯；食物库页面降低了重复录入成本；历史页面增强了长期回溯能力；饮水页面使健康管理范围更完整。"]),
("6.5 本章小结", [
"本章对系统功能、性能和运行结果进行了测试分析。测试结果表明，AIFood 能够满足毕业设计范围内的主要需求，但在自动化测试覆盖、云端同步和识别准确率方面仍有进一步提升空间。"])]),
("7 总结与展望", [
("7.1 工作总结", [
"本文设计并实现了一款基于 Android 的智能饮食记录与营养分析系统 AIFood。系统以个人健康管理为应用背景，围绕饮食记录、营养统计、食物库维护、饮水打卡、历史回溯和食物识别辅助录入等需求展开，实现了从数据录入到统计展示的完整闭环。",
"在技术实现上，系统采用 Java 17 和 Android 原生开发，使用 Room 完成本地持久化，使用 Repository 降低 UI 与数据库耦合，使用 AppExecutors 处理后台任务，使用 MPAndroidChart 展示趋势图，使用 CameraX、OkHttp 和 Gson 支撑识别扩展。通过分层设计，系统具备较好的可读性和可维护性。",
"在功能效果上，系统能够支持用户按日期和餐次记录饮食，自动完成营养换算，并以日、周、月维度呈现摄入情况。食物库和默认数据导入提升了记录效率，历史视图和饮水模块扩展了健康管理维度。"]),
("7.2 不足与展望", [
"当前系统仍存在一些不足。第一，核心数据主要保存在本地，换机或卸载后数据迁移不够方便；第二，食物识别依赖外部服务，识别结果仍需要用户确认，识别准确率和营养估计精度有待进一步验证；第三，自动化测试覆盖不足，部分页面交互仍依赖人工测试；第四，用户画像、运动消耗和个性化建议尚未深入实现。",
"后续工作可以从以下方面展开：接入用户登录与云同步，实现多设备数据一致；增加提醒通知和连续打卡统计，提高用户坚持记录的可能性；扩展食物数据库来源，提升数据覆盖面；优化识别模型和估重流程，减少用户手动输入；补充单元测试和界面自动化测试，提高系统长期维护质量。"]),
("7.3 本章小结", [
"总体而言，AIFood 完成了个人饮食记录与营养分析系统的主要功能，实现路线稳妥，结构清晰，具有进一步扩展为完整健康管理应用的基础。"])])
]


def add_chapters(doc):
    for ch, sections in chapters:
        add_heading(doc, ch, 1)
        for sec, paras in sections:
            add_heading(doc, sec, 2)
            for para in paras:
                if para.startswith("[[FIG:"):
                    _, rel_path, caption = para.strip("[]").split(":", 2)
                    path = ROOT / rel_path
                    if path.exists():
                        p = doc.add_paragraph()
                        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                        run = p.add_run()
                        run.add_picture(str(path), width=Cm(14))
                        add_text(doc, caption, align=WD_ALIGN_PARAGRAPH.CENTER, first_line=False)
                else:
                    add_text(doc, para)


def add_refs(doc):
    add_heading(doc, "参考文献", 1)
    refs = [
        "郭宏志. Android应用开发技术[M]. 北京: 电子工业出版社, 2021.",
        "王珊, 萨师煊. 数据库系统概论[M]. 北京: 高等教育出版社, 2014.",
        "Google. Guide to app architecture[EB/OL]. https://developer.android.com/topic/architecture, 2025.",
        "Google. Save data in a local database using Room[EB/OL]. https://developer.android.com/training/data-storage/room, 2025.",
        "Google. ViewModel overview[EB/OL]. https://developer.android.com/topic/libraries/architecture/viewmodel, 2025.",
        "Google. LiveData overview[EB/OL]. https://developer.android.com/topic/libraries/architecture/livedata, 2025.",
        "PhilJay. MPAndroidChart Documentation[EB/OL]. https://github.com/PhilJay/MPAndroidChart, 2025.",
        "Square. OkHttp Documentation[EB/OL]. https://square.github.io/okhttp/, 2025.",
        "Google. Gson User Guide[EB/OL]. https://github.com/google/gson, 2025.",
        "Google. CameraX overview[EB/OL]. https://developer.android.com/media/camera/camerax, 2025.",
        "中国营养学会. 中国居民膳食指南（2022）[M]. 北京: 人民卫生出版社, 2022.",
        "中国营养学会. 中国居民膳食营养素参考摄入量[M]. 北京: 科学出版社, 2023.",
        "陈明. 移动健康管理应用的设计与实现[J]. 软件工程, 2022, 25(6): 45-49.",
        "李华, 周强. 基于Android的个人健康管理系统研究[J]. 计算机应用与软件, 2021, 38(10): 120-125.",
        "刘洋. 基于Room数据库的Android本地数据持久化研究[J]. 信息技术与信息化, 2023(4): 88-91.",
        "WHO. Healthy diet[EB/OL]. https://www.who.int/news-room/fact-sheets/detail/healthy-diet, 2023.",
        "Dunford E, Trevena H, Goodsell C, et al. FoodSwitch: a mobile phone app to enable consumers to make healthier food choices and crowdsourcing of national food composition data[J]. JMIR mHealth and uHealth, 2014, 2(3): e37.",
        "Swan M. Emerging patient-driven health care models: an examination of health social networks, consumer personalized medicine and quantified self-tracking[J]. International Journal of Environmental Research and Public Health, 2009, 6(2): 492-525.",
    ]
    for i, ref in enumerate(refs, 1):
        add_text(doc, f"[{i}] {ref}", first_line=False)


def add_ack(doc):
    add_heading(doc, "致谢", 1)
    for t in [
        "本论文和系统开发工作是在指导教师的帮助下完成的。老师在选题确定、需求分析、系统设计、论文结构和修改完善等方面给予了耐心指导，使我能够较系统地完成 Android 应用的设计与实现。",
        "感谢学院各位老师在本科阶段课程学习中提供的知识基础，使我能够将数据库、移动开发、软件工程和程序设计等课程内容应用到本课题中。感谢同学在系统测试和论文修改过程中提出的意见。",
        "最后，感谢家人在学习和生活中的支持。由于个人能力和时间有限，论文和系统仍存在不足，恳请各位老师批评指正。",
    ]:
        add_text(doc, t)


def add_appendix(doc):
    add_heading(doc, "附录", 1)
    add_heading(doc, "附录A 系统主要运行截图说明", 2)
    images = [
        ("图A-1 首页日视图", ROOT / "运行图片" / "首页-日视图.jpg"),
        ("图A-2 饮食记录页面", ROOT / "运行图片" / "饮食记录页面.jpg"),
        ("图A-3 食物库页面", ROOT / "运行图片" / "食物库页面.jpg"),
        ("图A-4 饮水记录页面", ROOT / "运行图片" / "饮水记录页面.jpg"),
        ("图A-5 历史页月视图", ROOT / "运行图片" / "历史页-月视图.jpg"),
    ]
    for title, path in images:
        if path.exists():
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            run = p.add_run()
            run.add_picture(str(path), width=Cm(7))
            add_text(doc, title, align=WD_ALIGN_PARAGRAPH.CENTER, first_line=False)


def main():
    generate_figures()
    doc = Document()
    sec = doc.sections[0]
    sec.page_width = Cm(21)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(2.5)
    sec.bottom_margin = Cm(2.5)
    sec.left_margin = Cm(3)
    sec.right_margin = Cm(2.5)

    styles = doc.styles
    styles["Normal"].font.name = "Times New Roman"
    styles["Normal"]._element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
    styles["Normal"].font.size = Pt(12)

    cover(doc)
    abstract(doc)
    toc(doc)
    add_chapters(doc)
    add_refs(doc)
    add_ack(doc)
    add_appendix(doc)
    doc.save(OUT)
    print(OUT)


if __name__ == "__main__":
    main()
