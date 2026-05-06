package cn.dong.nexus;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication(scanBasePackages = {"cn.dong.nexus", "cn.dong.coade"})
@MapperScan({"cn.dong.nexus.modules.*.mapper", "cn.dong.coade.modules.*.mapper"})
@EnableScheduling
public class NexusApplication {
    @SneakyThrows
    static void main(String[] args) {
        SpringApplication app = new SpringApplication(NexusApplication.class);
        ConfigurableApplicationContext application = app.run(args);
        Environment env = application.getEnvironment();
        log.info("""
                        
                        ----------------------------------------------------------
                        \tApplication '{}' is running! Access URLs:
                        \tLocal: \t\thttp://localhost:{}
                        \tExternal: \thttp://{}:{}
                        \tDoc: \thttp://localhost:{}{}/doc.html
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.  "),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path") == null ? "" : env.getProperty("server.servlet.context-path"));

//        String weComId = "KD00720";
//        LocalDateTime localDateTime = LocalDateTime.of(2026, 4, 29, 12, 30);
//        AttendReissueApplyPassDTO attendReissueApplyPassDTO = new AttendReissueApplyPassDTO();
//        attendReissueApplyPassDTO.setEkpReviewId("19dc2f736e8e3b154bad7c344ceb680e");
//        attendReissueApplyPassDTO.setIsApproved(1);
//        SpringUtil.getBean(ICmtAttendService.class).doReissueAttend(attendReissueApplyPassDTO);
//        WeComApiUtil.addUserAttend("KD00720", LocalDateTime.of(2026, 4, 24, 12, 30));
//        for (int i = 1; i < 31; i++) {
//            SpringUtil.getBean(ICmtAttendRuleService.class).saveAllUserAttendRulesByDate(LocalDate.of(2026, 4, i));
//
//        }
//        EkpApprovalCurrentNodeBO currentApprovalNode = SpringUtil.getBean(CmtEkpService.class).getCurrentApprovalNode("19dfa9a1ed89c413e97ab294d59a91df");

    }

}
