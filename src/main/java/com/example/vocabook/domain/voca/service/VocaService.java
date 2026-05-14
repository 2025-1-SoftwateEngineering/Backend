package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.member.entity.mapping.MemberWord;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.member.repository.MemberVocaRepository;
import com.example.vocabook.domain.member.repository.MemberWordRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VocaService {

	private final VocaRepository vocaRepository;
	private final WordRepository wordRepository;
	private final MemberVocaRepository memberVocaRepository;
	private final MemberRepository memberRepository;
	private final MemberWordRepository memberWordRepository;

	// 단어장 전체 목록 조회
	@Transactional(readOnly = true)
	public VocaResDTO.VocaList getVocaList(AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		List<Voca> vocas = vocaRepository.findAll();
		List<Word> allWords = wordRepository.findByVocaIn(vocas);
		Map<Long, Long> wordCountMap = allWords.stream()
				.collect(java.util.stream.Collectors.groupingBy(w -> w.getVoca().getId(), java.util.stream.Collectors.counting()));
		Map<Long, Long> memorizedCountMap = memberWordRepository.countByMemberGroupByVoca(member, vocas).stream()
				.collect(java.util.stream.Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
		return VocaConverter.toVocaList(vocas, wordCountMap, memorizedCountMap);
	}

	// 암기한 단어 저장
	@Transactional
	public VocaResDTO.MemorizeInfo memorizeWords(Long vocaId, AuthMember authMember, VocaReqDTO.Memorize dto) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		List<Word> words = wordRepository.findAllById(dto.getWordIds());

		java.util.Set<Long> alreadyMemorized = memberWordRepository.findWordIdsByMemberAndWordIn(member, words);
		words.stream()
				.filter(w -> !alreadyMemorized.contains(w.getId()))
				.forEach(w -> memberWordRepository.save(MemberWord.builder().member(member).word(w).build()));

		List<Long> allMemorizedIds = memberWordRepository.findWordIdsByMemberAndVocaId(member, vocaId);
		return VocaResDTO.MemorizeInfo.builder()
				.memorizedWordIds(allMemorizedIds)
				.totalCount(allMemorizedIds.size())
				.build();
	}

	// 암기한 단어 목록 조회
	@Transactional(readOnly = true)
	public VocaResDTO.MemorizeInfo getMemorizedWords(Long vocaId, AuthMember authMember) {
		vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		List<Long> memorizedWordIds = memberWordRepository.findWordIdsByMemberAndVocaId(member, vocaId);
		return VocaResDTO.MemorizeInfo.builder()
				.memorizedWordIds(memorizedWordIds)
				.totalCount(memorizedWordIds.size())
				.build();
	}

	// 학습한 단어장 목록 조회
	@Transactional(readOnly = true)
	public VocaResDTO.StudiedVocaList getStudiedVocas(AuthMember authMember) {
		Member member = authMember.getMember();
		List<MemberVoca> memberVocas = memberVocaRepository.findAllByMember(member);
		return VocaConverter.toStudiedVocaList(memberVocas);
	}

	public VocaResDTO.WordList getWords(Long vocaId, int page, int pageSize) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));
		Page<Word> wordPage = wordRepository.findByVoca(voca, PageRequest.of(page, pageSize));
		return VocaConverter.toWordList(voca, wordPage);
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
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VoceErrorCode.VOCA_NOT_FOUND));

		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		boolean alreadySubmittedToday = memberVocaRepository.findByMemberAndVoca(member, voca)
				.map(mv -> mv.getSolvedAt() != null && mv.getSolvedAt().toLocalDate().isEqual(today))
				.orElse(false);

		List<Long> wordIds = dto.getAnswers().stream()
				.map(VocaReqDTO.SubmitTest.Answer::getWordId)
				.toList();
		Map<Long, Word> wordMap = wordRepository.findAllById(wordIds).stream()
				.collect(java.util.stream.Collectors.toMap(Word::getId, w -> w));

		List<VocaResDTO.AnswerResult> results = new ArrayList<>();
		int correctCount = 0;

		for (VocaReqDTO.SubmitTest.Answer answer : dto.getAnswers()) {
			Word word = Optional.ofNullable(wordMap.get(answer.getWordId()))
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

		long earnedCoins = 0;
		if (!alreadySubmittedToday) {
			earnedCoins = (long) correctCount * 5;
			member.addCoin(earnedCoins);
			member.updateStreak();
			if (member.getStreak() % 7 == 0) {
				member.addCoin(500);
				earnedCoins += 500;
			}
		} else {
			earnedCoins = (long) correctCount * 3;
			member.addCoin(earnedCoins);
		}
		memberRepository.saveAndFlush(member);
		saveMemberVoca(member, voca, (long) correctCount, (long) dto.getAnswers().size());

		return VocaResDTO.TestResult.builder()
				.totalCount(dto.getAnswers().size())
				.correctCount(correctCount)
				.wrongCount(dto.getAnswers().size() - correctCount)
				.earnedCoins(earnedCoins)
				.results(results)
				.build();
	}

	private void saveMemberVoca(Member member, Voca voca, Long correctCnt, Long learningWordCnt) {
		memberVocaRepository.findByMemberAndVoca(member, voca)
				.ifPresentOrElse(
						memberVoca -> memberVoca.updateResult(correctCnt, learningWordCnt, LocalDateTime.now()),
						() -> memberVocaRepository.save(MemberVoca.builder()
								.member(member)
								.voca(voca)
								.correctCnt(correctCnt)
								.learningWordCnt(learningWordCnt)
								.solvedAt(LocalDateTime.now())
								.build())
				);
	}
}
