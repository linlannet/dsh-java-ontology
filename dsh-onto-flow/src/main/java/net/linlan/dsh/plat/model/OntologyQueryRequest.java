package net.linlan.dsh.plat.model;

import jakarta.validation.constraints.NotBlank;

public record OntologyQueryRequest(
        @NotBlank String ontologyId,
        @NotBlank String query
) {
}
