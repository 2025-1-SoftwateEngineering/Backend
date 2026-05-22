package com.example.vocabook.domain.pet.controller;

import com.example.vocabook.domain.pet.code.PetSuccessCode;
import com.example.vocabook.domain.pet.controller.docs.PetControllerDocs;
import com.example.vocabook.domain.pet.dto.PetResDTO;
import com.example.vocabook.domain.pet.service.PetService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pets")
public class PetController implements PetControllerDocs {

	private final PetService petService;

	@GetMapping("/me")
	public ApiResponse<PetResDTO.PetInfo> getPet(
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(PetSuccessCode.GET_PET, petService.getPet(authMember));
	}

	@PostMapping
	public ApiResponse<PetResDTO.PetInfo> createPet(
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(PetSuccessCode.CREATE_PET, petService.createPet(authMember));
	}
}
