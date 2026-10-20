package net.linlan.dsh.metric.service;

import net.linlan.dsh.metric.entity.*;
import net.linlan.dsh.metric.repository.MetricRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 指标层服务类
 * @author Linlan
 * CreatedAt 2026-10-20
 * @version 1.0
 * @since 1.0
 */
@Service
public class MetricService {

    private final MetricRepository repository;

    public MetricService(MetricRepository repository) {
        this.repository = repository;
    }

    public List<MetricBusinessObject> findAllBusinessObjects(Long tenantId) {
        return repository.findAllBusinessObjects(tenantId);
    }

    public MetricBusinessObject findBusinessObjectById(Long id, Long tenantId) {
        return repository.findBusinessObjectById(id, tenantId);
    }

    public long createBusinessObject(MetricBusinessObject o) {
        if (o.getStatus() == null) o.setStatus(0);
        return repository.insertBusinessObject(o);
    }

    public List<MetricProcess> findAllProcesses(Long tenantId) {
        return repository.findAllProcesses(tenantId);
    }

    public List<MetricProcess> findProcessesByBusinessObjectId(Long businessObjectId) {
        return repository.findProcessesByBusinessObjectId(businessObjectId);
    }

    public long createProcess(MetricProcess p) {
        if (p.getStatus() == null) p.setStatus(0);
        return repository.insertProcess(p);
    }

    public List<MetricDecisionPoint> findAllDecisionPoints(Long tenantId) {
        return repository.findAllDecisionPoints(tenantId);
    }

    public List<MetricDecisionPoint> findDecisionPointsByProcessId(Long processId) {
        return repository.findDecisionPointsByProcessId(processId);
    }

    public long createDecisionPoint(MetricDecisionPoint d) {
        if (d.getStatus() == null) d.setStatus(0);
        if (d.getNodeIndex() == null) d.setNodeIndex(0);
        return repository.insertDecisionPoint(d);
    }

    public List<MetricDefinition> findAllDefinitions(Long tenantId) {
        return repository.findAllDefinitions(tenantId);
    }

    public List<MetricDefinition> findDefinitionsByCategory(String category, Long tenantId) {
        return repository.findDefinitionsByCategory(category, tenantId);
    }

    public MetricDefinition findDefinitionById(Long id, Long tenantId) {
        return repository.findDefinitionById(id, tenantId);
    }

    public long createDefinition(MetricDefinition m) {
        if (m.getStatus() == null) m.setStatus(0);
        return repository.insertDefinition(m);
    }

    public List<MetricDependency> findAllDependencies(Long tenantId) {
        return repository.findAllDependencies(tenantId);
    }

    public long createDependency(MetricDependency d) {
        if (d.getDependencyType() == null) d.setDependencyType("DIRECT");
        return repository.insertDependency(d);
    }

    /**
     * 构建指标依赖树：以指定指标为根，递归展开其全部上游依赖（被依赖方）。
     * 用于展示一个复合/派生指标由哪些底层指标计算而来。
     *
     * @param rootMetricId 根指标ID（通常为复合或派生指标）
     * @param tenantId 租户ID
     * @return 依赖树根节点
     */
    public MetricTreeNode buildDependencyTree(Long rootMetricId, Long tenantId) {
        List<MetricDependency> allDeps = repository.findAllDependencies(tenantId);
        Map<Long, List<Long>> upstreamMap = new HashMap<>();
        for (MetricDependency dep : allDeps) {
            upstreamMap.computeIfAbsent(dep.getDownstreamMetricId(), k -> new ArrayList<>())
                    .add(dep.getUpstreamMetricId());
        }
        MetricTreeNode root = new MetricTreeNode(rootMetricId, 0);
        Deque<MetricTreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        Set<Long> visited = new HashSet<>();
        visited.add(rootMetricId);
        while (!queue.isEmpty()) {
            MetricTreeNode node = queue.poll();
            List<Long> upstreams = upstreamMap.get(node.metricId);
            if (upstreams != null) {
                for (Long upId : upstreams) {
                    MetricTreeNode child = new MetricTreeNode(upId, node.depth + 1);
                    node.children.add(child);
                    if (visited.add(upId)) {
                        queue.add(child);
                    }
                }
            }
        }
        return root;
    }

    /**
     * 指标依赖树节点
     */
    public static class MetricTreeNode {
        public final Long metricId;
        public final int depth;
        public final List<MetricTreeNode> children = new ArrayList<>();

        public MetricTreeNode(Long metricId, int depth) {
            this.metricId = metricId;
            this.depth = depth;
        }
    }
}
