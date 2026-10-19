package net.linlan.dsh.workspace.controller;

import net.linlan.dsh.common.controller.GenericCrudController;
import net.linlan.dsh.workspace.service.WorkspaceCrudService;
import org.springframework.web.bind.annotation.*;

/**
 * 知识沉淀域CRUD读写控制类
 * <p>P2-16：收敛至 {@link GenericCrudController} 基类，仅声明路径前缀和 Service。
 * @author Linlan
 * CreatedAt 2026-10-13
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController extends GenericCrudController {

    private final WorkspaceCrudService workspaceService;

    public WorkspaceController(WorkspaceCrudService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    protected WorkspaceCrudService crudService() {
        return workspaceService;
    }
}
