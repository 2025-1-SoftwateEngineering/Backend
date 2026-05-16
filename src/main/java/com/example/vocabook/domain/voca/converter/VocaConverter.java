package com.example.vocabook.domain.voca.converter;

import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Choice;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class VocaConverter {

    public static Voca toVoca(
            AdminReqDTO.AddVocabulary dto
    ){
        return Voca.builder()
                .createdAt(LocalDateTime.now())
                .description(dto.description())
                .solvedCoin(dto.solvedCoin())
                .build();
    }

	public static VocaResDTO.WordList toWordList(Voca voca, Page<Word> wordPage) {
		List<VocaResDTO.WordInfo> words = wordPage.getContent().stream()
				.map(word -> VocaResDTO.WordInfo.builder()
						.wordId(word.getId())
						.englishWord(word.getEnglishWord())
						.meaning(word.getMeaning())
						.build())
				.toList();
		return VocaResDTO.WordList.builder()
				.vocaId(voca.getId())
				.description(voca.getDescription())
				.words(words)
				.totalPages(wordPage.getTotalPages())
				.totalElements(wordPage.getTotalElements())
				.build();
	}

	public static VocaResDTO.TestQuestion toTestQuestion(Word word) {
		return VocaResDTO.TestQuestion.builder()
				.wordId(word.getId())
				.englishWord(word.getEnglishWord())
				.meaning(word.getMeaning())
				.build();
	}

	// 단어장 목록 변환
	public static VocaResDTO.VocaList toVocaList(List<Voca> vocas, Map<Long, Long> wordCountMap, Map<Long, Long> memorizedCountMap) {
		List<VocaResDTO.VocaInfo> vocaInfos = vocas.stream()
				.map(v -> VocaResDTO.VocaInfo.builder()
						.vocaId(v.getId())
						.description(v.getDescription())
						.wordCount(wordCountMap.getOrDefault(v.getId(), 0L))
						.memorizedCount(memorizedCountMap.getOrDefault(v.getId(), 0L))
						.createdAt(v.getCreatedAt())
						.build())
				.toList();
		return VocaResDTO.VocaList.builder()
				.vocas(vocaInfos)
				.totalCount(vocaInfos.size())
				.build();
	}

	// 학습한 단어장 목록 변환
	public static VocaResDTO.StudiedVocaList toStudiedVocaList(List<MemberVoca> memberVocas) {
		List<VocaResDTO.StudiedVocaInfo> vocas = memberVocas.stream()
				.map(mv -> VocaResDTO.StudiedVocaInfo.builder()
						.vocaId(mv.getVoca().getId())
						.description(mv.getVoca().getDescription())
						.learningWordCnt(mv.getLearningWordCnt())
						.correctCnt(mv.getCorrectCnt())
						.solvedAt(mv.getSolvedAt())
						.build())
				.toList();
		return VocaResDTO.StudiedVocaList.builder()
				.vocas(vocas)
				.totalCount(vocas.size())
				.build();
	}

    // 사지선다 문제 목록 조회
    public static VocaResDTO.GetChoiceList toGetChoiceList(
            Choice choice,
            Long cnt
    ) {
        return VocaResDTO.GetChoiceList.builder()
                .id(choice.getId())
                .solvedCoin(choice.getSolvedCoin())
                .cnt(cnt)
                .build();
    }

    // 사지선다 선택지 조회
    public static VocaResDTO.GetChoice toGetChoice(
            Long id,
            Long score,
            String question,
            List<VocaResDTO.ChoiceElement> choices
    ){
        return VocaResDTO.GetChoice.builder()
                .id(id)
                .score(score)
                .question(question)
                .choices(choices)
                .build();
    }

    public static VocaResDTO.ChoiceElement toChoiceElement(
            Long id,
            String word
    ){
        return VocaResDTO.ChoiceElement.builder()
                .id(id)
                .text(word)
                .build();
    }

    // 사지선다 정답 제출
    public static VocaResDTO.SubmitChoice toSubmitChoice(
            Boolean isCorrect,
            Boolean hasNext,
            Long nextCurrent,
            Long score
    ) {
        return VocaResDTO.SubmitChoice.builder()
                .isCorrect(isCorrect)
                .hasNext(hasNext)
                .nextCurrent(nextCurrent)
                .score(score)
                .build();
    }
}
