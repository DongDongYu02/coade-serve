package cn.dong.nexus.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nexus")
@Tag(name = "Nexus-Boot")
public class NexusController {


    @GetMapping
    @Operation(summary = "Nexus-Boot")
    public String index() {
        return "Hello Nexus-Boot";
    }
}
