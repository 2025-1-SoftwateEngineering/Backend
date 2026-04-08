package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.member.repository.MemberVocaRepository;
import com.example.vocabook.domain.voca.converter.VocaConverter;
import com.example.vocabook.domain.voca.dto.VocaReqDTO;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.exception.VocaException;
import com.example.vocabook.domain.voca.exception.code.VoceErrorCode;
import com.example.vocabook.domain.voca.repository.VocaRepository;
import com.example.vocabook.domain.voca.repository.WordRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocaService {

	private final VocaRepository vocaRepository;
	private final WordRepository wordRepository;
	private final MemberVocaRepository memberVocaRepository;

	public VocaResDTO.WordList getWords(Long vocaId, int page) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		Page<Word> wordPage = wordRepository.findByVoca(voca, PageRequest.of(page, 10));
		return VocaConverter.toWordList(wordPage);
	}

	public List<VocaResDTO.TestQuestion> getTestQuestions(Long vocaId) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		List<Word> words = new ArrayList<>(wordRepository.findByVoca(voca));
		Collections.shuffle(words);
		return words.stream()
				.map(VocaConverter::toTestQuestion)
				.toList();
	}

	@Transactional
	public VocaResDTO.TestResult submitTest(Long vocaId, AuthMember authMember, VocaReqDTO.SubmitTest dto) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		Member member = authMember.getMember();

		List<VocaResDTO.AnswerResult> results = new ArrayList<>();
		int correctCount = 0;

		for (VocaReqDTO.SubmitTest.Answer answer : dto.getAnswers()) {
			Word word = wordRepository.findById(answer.getWordId())
					.orElseThrow(() -> new VocaException(VoceErrorCode.WORD_NOT_FOUND));
			boolean isCorrect = word.getEnglishWord().equalsIgnoreCase(answer.getAnswer());
			if (isCorrect) correctCount++;

			results.add(VocaResDTO.AnswerResult.builder()
					.wordId(word.getId())
					.meaning(word.getMeaning())
					.correctAnswer(word.getEnglishWord())
					.submittedAnswer(answer.getAnswer())
					.correct(isCorrect)
					.build());
		}

		saveMemberVoca(member, voca, (long) correctCount, (long) dto.getAnswers().size());

		return VocaResDTO.TestResult.builder()
				.totalCount(dto.getAnswers().size())
				.correctCount(correctCount)
				.results(results)
				.build();
	}

	private void saveMemberVoca(Member member, Voca voca, Long correctCnt, Long learningWordCnt) {
		if (memberVocaRepository.findByMemberAndVoca(member, voca).isPresent()) {
			memberVocaRepository.updateResult(member, voca, correctCnt, learningWordCnt, LocalDateTime.now());
		} else {
			memberVocaRepository.save(MemberVoca.builder()
					.member(member)
					.voca(voca)
					.correctCnt(correctCnt)
					.learningWordCnt(learningWordCnt)
					.solvedAt(LocalDateTime.now())
					.build());
		}
	}
}