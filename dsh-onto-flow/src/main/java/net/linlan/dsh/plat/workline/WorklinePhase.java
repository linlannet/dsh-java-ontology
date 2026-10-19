package net.linlan.dsh.plat.workline;

public enum WorklinePhase {

    TOPIC_SETUP(1, "主题设定"),
    COLLECTION_MONITORING(2, "采集监测"),
    NLP_ANALYSIS(3, "NLP分析"),
    KG_CONSTRUCTION(4, "知识图谱构建"),
    HOTSPOT_ANALYSIS(5, "热点分析"),
    REPORT_GENERATION(6, "报告生成"),
    PUSH_DISPLAY(7, "推送展示");

    private final int order;
    private final String name;

    WorklinePhase(int order, String name) {
        this.order = order;
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public static WorklinePhase fromOrder(int order) {
        for (WorklinePhase phase : values()) {
            if (phase.order == order) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown phase order: " + order);
    }
}
