package com.example.ott.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ott.dto.ReportDTO;
import com.example.ott.entity.Report;
import com.example.ott.service.ReportService;
import com.example.ott.type.Reason;
import com.example.ott.type.Status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Controller

@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    /** 목록 페이지: /report/list */
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) List<Reason> reasons,
            @RequestParam(required = false) List<Status> statuses,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC, size = 20) Pageable pageable,
            Model model) {
        Page<ReportDTO> page = reportService.getReports(reasons, statuses, pageable);

        model.addAttribute("page", page);
        model.addAttribute("reasons", reasons);
        model.addAttribute("statuses", statuses);
        return "report/reportlist"; // 뷰 템플릿
    }

    /** 신고 생성: POST /report (JSON 본문: ReportDTO) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody ReportDTO dto) {
        Report saved = reportService.createReport(dto);
        ReportDTO body = reportService.entityToDto(saved);
        return ResponseEntity
                .created(URI.create("/report/" + saved.getId())) // Location 헤더
                .body(body);
    }

    /**
     * 신고 상태 업데이트: PATCH /report/{id}/status (JSON 본문: UpdateReportStatusRequest)
     */
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> updateReportStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusRequest req) {
        ReportDTO dto = ReportDTO.builder()
                .id(id)
                .status(req.getStatus())
                .reason(req.getReason())
                .build();
        reportService.updateReportStatus(dto);
        return ResponseEntity.noContent().build(); // 204
    }

    @Data
    public static class UpdateReportStatusRequest {
        @NotNull
        private Status status; // 필수
        private Reason reason; // 선택
    }

    @Data
    public static class ErrorResponse {
        private final String code;
        private final String message;
    }

}
