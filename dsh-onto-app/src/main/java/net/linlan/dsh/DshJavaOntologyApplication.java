package net.linlan.dsh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 聚合应用入口：打包 base/flow/report/visulization 四模块
 * <p>主类位于 {@code net.linlan.dsh} 根包，默认组件扫描即覆盖全部模块与
 * 应用层 {@code net.linlan.dsh.onto.*}（config/model/service/controller 分层，
 * 参考 dsh-java-mysql-app 结构）。</p>
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableScheduling
public class DshJavaOntologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DshJavaOntologyApplication.class, args);
    }
}
