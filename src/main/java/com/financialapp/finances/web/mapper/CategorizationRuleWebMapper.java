package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.model.rule.RuleMatchType;
import com.financialapp.finances.domain.usecase.rule.CategorySuggestion;
import com.financialapp.finances.domain.usecase.rule.RulePreviewResult;
import com.financialapp.finances.domain.usecase.rule.RuleView;
import com.financialapp.finances.domain.usecase.rule.command.CreateCategorizationRuleCommand;
import com.financialapp.finances.web.dto.request.CreateCategorizationRuleRequest;
import com.financialapp.finances.web.dto.response.CategorizationRuleResponse;
import com.financialapp.finances.web.dto.response.CategorySuggestionResponse;
import com.financialapp.finances.web.dto.response.RulePreviewResponse;
import org.springframework.stereotype.Component;

@Component
public class CategorizationRuleWebMapper {

    public CreateCategorizationRuleCommand toCreateCommand(Long userId, CreateCategorizationRuleRequest req) {
        return new CreateCategorizationRuleCommand(
                new UserId(userId),
                RuleMatchType.valueOf(req.matchType()),
                req.pattern(),
                new CategoryId(req.categoryId())
        );
    }

    public CategorizationRuleResponse toRuleResponse(RuleView view) {
        CategorizationRule r = view.rule();
        return new CategorizationRuleResponse(
                r.id().value(),
                r.matchType().name(),
                r.pattern(),
                r.categoryId().value(),
                view.categoryName(),
                r.matchCount(),
                r.createdAt()
        );
    }

    public RulePreviewResponse toPreviewResponse(RulePreviewResult result) {
        return new RulePreviewResponse(result.wouldMatchCount(), result.sampleTransactionIds());
    }

    public CategorySuggestionResponse toSuggestionResponse(CategorySuggestion suggestion) {
        Long catId = suggestion.categoryId() != null ? suggestion.categoryId().value() : null;
        return new CategorySuggestionResponse(suggestion.description(), catId);
    }
}
