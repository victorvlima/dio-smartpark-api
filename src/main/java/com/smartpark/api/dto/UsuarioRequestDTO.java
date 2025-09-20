package com.smartpark.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "O username é obrigatório.")
    @Size(min = 3, max = 50, message = "O username deve ter entre 3 e 50 caracteres.")
    private String username;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String password;

    @NotBlank(message = "A role é obrigatória.")
    @Size(max = 20, message = "A role deve ter no máximo 20 caracteres.")
    private String role; // Ex: "ADMIN", "USER"
}