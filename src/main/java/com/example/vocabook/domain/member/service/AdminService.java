package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.AdminErrorCode;
import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.converter.AdminConverter;
import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.member.dto.res.AdminResDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.PetImage;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.member.entity.dto.ReportDTO;
import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import com.example.vocabook.domain.member.enums.PhotoType;
import com.example.vocabook.domain.member.exception.AdminException;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.member.repository.PetImageRepository;
import com.example.vocabook.domain.member.repository.ReportRepository;
import com.example.vocabook.domain.pet.repository.MemberPetRepository;
import com.example.vocabook.domain.store.code.StoreErrorCode;
import com.example.vocabook.domain.store.converter.StoreConverter;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.repository.ItemRepository;
import com.example.vocabook.domain.voca.converter.ChoiceConverter;
import com.example.vocabook.domain.voca.converter.CrosswordConverter;
import com.example.vocabook.domain.voca.converter.VocaConverter;
import com.example.vocabook.domain.voca.converter.WordConverter;
import com.example.vocabook.domain.voca.entity.Choice;
import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.ChoiceQuestion;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import com.example.vocabook.domain.voca.repository.*;
import com.example.vocabook.global.apiPayload.code.GeneralErrorCode;
import com.example.vocabook.global.apiPayload.converter.PagingConverter;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.util.GcsUtil;
import com.google.cloud.storage.Blob;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final VocaRepository vocaRepository;
    private final WordRepository wordRepository;
    private final ChoiceRepository choiceRepository;
    private final ChoiceQuestionRepository choiceQuestionRepository;
    private final CrosswordRepository crosswordRepository;
    private final CrosswordHintRepository crosswordHintRepository;
    private final GcsUtil gcsUtil;
    private final ItemRepository itemRepository;
    private final MemberPetRepository memberPetRepository;
    private final PetImageRepository petImageRepository;

    // 신고 목록 조회
    public PagingResDTO.Cursor<AdminResDTO.ReportList> getReportList(
            String cursor,
            Integer pageSize
    ) {

        // Pageable
        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        // 커서구조 = count:id
        Slice<ReportDTO.ReportCnt> reportList;
        if (cursor.equals("-1")) {
            reportList = reportRepository.findReportListWithoutCursor(pageRequest);
        } else {
            Long countCursor;
            Long idCursor;
            try {
                String[] split = cursor.split(":");
                countCursor = Long.parseLong(split[0]);
                idCursor = Long.parseLong(split[1]);
            } catch (Exception e) {
                throw new MemberException(GeneralErrorCode.INVADED_CURSOR);
            }

            reportList = reportRepository.findReportListWithCursor(countCursor, idCursor, pageRequest);
        }

        if (reportList.isEmpty()){
            return PagingConverter.toCursor(null, null, false, 0);
        }

        // 커서 제작
        ReportDTO.ReportCnt lastElement = reportList.getContent().getLast();
        String nextCursor = lastElement.cnt() + ":" + lastElement.report().getId();

        return PagingConverter.toCursor(
                reportList.stream()
                        .map(i -> AdminConverter.toReportList(i.report(), i.cnt()))
                        .toList(),
                nextCursor,
                reportList.hasNext(),
                pageSize
        );
    }

    // 영구 정지
    @Transactional
    public AdminResDTO.Suspend suspend(
            Long memberId
    ) {

        // 정지시킬 사용자 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 영구 정지 처리
        member.suspend();

        // 그동안 신고당한 거 삭제
        List<Report> reportList = reportRepository.findAllByTargetMember(member);
        reportRepository.deleteAll(reportList);

        return AdminConverter.toSuspend(member);
    }

    // 단어장 추가
    @Transactional
    public List<AdminResDTO.AddVocabulary> addVocabulary(
            List<AdminReqDTO.AddVocabulary> dto
    ) {
        // 1. 이미 존재하는 단어장들을 한 번에 조회하기 위해 description 목록 추출
        List<String> descriptions = dto.stream()
                .map(AdminReqDTO.AddVocabulary::description)
                .distinct()
                .toList();

        // 2. 추출한 description으로 DB에 존재하는 단어장 목록 조회 후 Set으로 변환
        Set<String> existingVocaPairs = vocaRepository.findByDescriptionIn(descriptions).stream()
                .map(v -> v.getDescription() + ":" + v.getSolvedCoin())
                .collect(Collectors.toSet());

        // 3. 단어장 추가 목록 생성 (기존에 없는 단어장만 필터링)
        List<Voca> vocaList = new ArrayList<>();
        dto.forEach(i -> {
            String vocaKey = i.description() + ":" + i.solvedCoin();
            
            if (!existingVocaPairs.contains(vocaKey)) {
                vocaList.add(VocaConverter.toVoca(i));
                
                // 중복 추가 방지를 위해 Set에 임시 추가
                existingVocaPairs.add(vocaKey);
            }
        });

        // 4. 단어장 저장
        List<Voca> savedVocaList = vocaRepository.saveAll(vocaList);

        return savedVocaList.stream()
                .map(AdminConverter::toAddVocabulary)
                .toList();
    }

    // 단어장 목록 조회
    public PagingResDTO.Cursor<AdminResDTO.GetVocabularyList> getVocabularyList(
            String cursor,
            Integer pageSize
    ) {

        // Pageable
        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        Slice<Voca> vocaList;
        if (!cursor.equals("-1")){
            Long idCursor;
            try {
                idCursor = Long.parseLong(cursor);
            } catch (Exception e) {
                throw new MemberException(GeneralErrorCode.INVADED_CURSOR);
            }
            vocaList = vocaRepository.findVocaListWithCursor(idCursor, pageRequest);
        } else {
            vocaList = vocaRepository.findVocaListWithoutCursor(pageRequest);
        }

        if (vocaList.isEmpty()) {
            return PagingConverter.toCursor(null, null, false, 0);
        }

        // Voca에 연관된 단어들 한번에 조회 (N+1 문제 방지)
        List<Word> words = wordRepository.findByVocaIn(vocaList.getContent());
        Map<Long, List<Word>> wordMap = words.stream()
                .collect(Collectors.groupingBy(w -> w.getVoca().getId()));

        String nextCursor = String.valueOf(vocaList.getContent().getLast().getId());

        return PagingConverter.toCursor(
                vocaList.stream()
                        .map(voca -> AdminConverter.toGetVocabularyList(voca, wordMap.getOrDefault(voca.getId(), new ArrayList<>())))
                        .toList(),
                nextCursor,
                vocaList.hasNext(),
                pageSize
        );
    }

    // 단어 생성
    @Transactional
    public List<AdminResDTO.AddWord> addWord(
            List<AdminReqDTO.AddWord> dto
    ) {
        // 1. 필요한 단어장(Voca)들을 한 번에 조회
        List<Long> vocaIds = dto.stream()
                .map(AdminReqDTO.AddWord::vocabularyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Voca> vocaMap = vocaRepository.findAllById(vocaIds).stream()
                .collect(Collectors.toMap(Voca::getId, v -> v));

        // 2. 이미 존재하는 단어들을 한 번에 조회하기 위해 영어 단어 목록 추출
        List<String> englishWords = dto.stream()
                .map(AdminReqDTO.AddWord::english)
                .map(String::toLowerCase)
                .distinct()
                .toList();

        // 3. 추출한 영어 단어로 DB에 존재하는 단어 목록 조회 후 Set으로 변환 (빠른 조회를 위함)
        Set<String> existingWordPairs = wordRepository.findByEnglishWordInIgnoreCase(englishWords).stream()
                .map(w -> w.getEnglishWord().toLowerCase() + ":" + w.getMeaning())
                .collect(Collectors.toSet());

        // 4. 단어 추가 목록 생성 (기존에 없는 단어만 필터링)
        List<Word> wordList = new ArrayList<>();
        dto.forEach(i -> {
            String wordKey = i.english().toLowerCase() + ":" + i.meaning();
            
            if (!existingWordPairs.contains(wordKey)) {
                Voca voca = null;
                if (i.vocabularyId() != null) {
                    voca = vocaMap.get(i.vocabularyId());
                }

                if (voca != null) {
                    wordList.add(WordConverter.toWord(i, voca));
                } else {
                    wordList.add(WordConverter.toWord(i));
                }
                
                // 중복 추가 방지를 위해 Set에 임시 추가
                existingWordPairs.add(wordKey);
            }
        });

        // 5. 생성된 단어들 일괄 저장
        List<Word> savedWordList = wordRepository.saveAll(wordList);

        return savedWordList.stream()
                .map(AdminConverter::toAddWord)
                .toList();
    }

    // 단어 검색
    public AdminResDTO.SearchWord searchWord(
            String word
    ) {
        Word foundWord = wordRepository.findFirstByEnglishWordIgnoreCase(word).orElse(null);
        return AdminConverter.toSearchWord(foundWord);
    }

    // 단어장 수정
    @Transactional
    public AdminResDTO.UpdateVocabulary updateVocabulary(
            Long vocaId,
            AdminReqDTO.UpdateVocabulary dto
    ) {
        Voca voca = vocaRepository.findById(vocaId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.VOCA_NOT_FOUND));

        voca.update(dto.description(), dto.solvedCoin());
        return AdminConverter.toUpdateVocabulary(voca);
    }

    // 단어장 삭제
    @Transactional
    public AdminResDTO.DeleteVocabulary deleteVocabulary(
            Long vocaId
    ) {
        Voca voca = vocaRepository.findById(vocaId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.VOCA_NOT_FOUND));

        vocaRepository.delete(voca);
        return AdminConverter.toDeleteVocabulary(vocaId);
    }

    // 단어 수정
    @Transactional
    public AdminResDTO.UpdateWord updateWord(
            Long wordId,
            AdminReqDTO.UpdateWord dto
    ) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.WORD_NOT_FOUND));

        Voca voca = null;
        if (dto.vocabularyId() != null) {
            voca = vocaRepository.findById(dto.vocabularyId())
                    .orElseThrow(() -> new AdminException(AdminErrorCode.VOCA_NOT_FOUND));
        }

        word.update(dto.english(), dto.meaning(), voca);
        return AdminConverter.toUpdateWord(word);
    }

    // 단어 삭제
    @Transactional
    public AdminResDTO.DeleteWord deleteWord(
            Long wordId
    ) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new AdminException(AdminErrorCode.WORD_NOT_FOUND));

        wordRepository.delete(word);
        return AdminConverter.toDeleteWord(wordId);
    }

    // 사지선다 문제 생성
    @Transactional
    public List<AdminResDTO.CreateChoice> createChoice(
            AdminReqDTO.CreateChoice dto
    ) {
        // 단어들 리스트화 (단어랑 뜻 함께 있음)
        List<String> answerList = dto.choices().stream()
                .map(AdminReqDTO.ChoiceList::word)
                .map(String::toLowerCase)
                .toList();

        // 단어 조회 (단어 or 뜻 둘 중 하나중에 있다면)
        List<Word> wordList = wordRepository.findAllByEnglishWordInIgnoreCaseOrMeaningIn(answerList, answerList);

        // 조회된 단어가 없는 경우
        if (wordList.isEmpty()) {
            throw new AdminException(AdminErrorCode.WORD_NOT_FOUND);
        }

        // 사지선다 생성 & 저장
        Choice choice = ChoiceConverter.toChoice(dto.solvedCoin());
        choiceRepository.save(choice);

        // 사지선다 질문 생성 & 저장
        List<ChoiceQuestion> choiceQuestionList = new ArrayList<>();
        wordList.forEach(w -> {
            // DTO에 담겨있는 단어인지 확인
            dto.choices().forEach(c -> {
                if (c.word().equalsIgnoreCase(w.getEnglishWord()) || c.word().equals(w.getMeaning())) {
                    choiceQuestionList.add(ChoiceConverter.toChoiceQuestion(choice, w, c.isWord()));
                }
            });
        });

        List<ChoiceQuestion> result = choiceQuestionRepository.saveAll(choiceQuestionList);

        return result.stream()
                .map(AdminConverter::toCreateChoice)
                .toList();
    }

    // 십자말풀이 문제 생성
    @Transactional
    public AdminResDTO.CreateCrossword createCrosswords(
            AdminReqDTO.CreateCrossword dto
    ) {

        // 십자말풀이 생성
        Crossword crossword = crosswordRepository.save(CrosswordConverter.toCrossword(dto.solvedCoin()));

        // 십자말풀이 힌트 생성
        List<CrosswordHint> crosswordHintList = dto.crosswords().stream()
                .map(c -> {
                    Optional<Word> word = wordRepository.findFirstByEnglishWordIgnoreCase(c.word());

                    // 만약 어느 하나라도 start_point 형식이 다르다면
                    if (!c.wordStartPoint().matches("\\d+\\s\\d+")){
                        throw new AdminException(AdminErrorCode.INVADE_START_REGEX);
                    }

                    return word.map(value -> CrosswordConverter.toCrosswordHint(
                            crossword,
                            c.clueDescription(),
                            c.clueType(),
                            c.wordStartPoint(),
                            value
                    )).orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        crosswordHintRepository.saveAll(crosswordHintList);

        return AdminConverter.toCreateCrossword(crossword);
    }

    // 사진 업로드용 URL 발급
    public AdminResDTO.CreateSignedUri createSignedUri(
            String fileName,
            PhotoType photoType
    ) {
        // 확장자가 있는지 검증
        if (fileName.lastIndexOf(".") == -1) {
            throw new AdminException(AdminErrorCode.INVADE_PHOTO_TYPE);
        }

        // 확장자 추출
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        // UUID로 파일명 다시 조립
        String uuid = java.util.UUID.randomUUID().toString();

        // 권한 & 사진 타입에 따라 프리픽스 변경
        String prefix = switch(photoType){
            case PROFILE -> "profile/";
            case ITEM -> "item/";
            case BACKGROUND -> "background/";
            case PET -> "pet/";
        };

        String url = gcsUtil.createSignedUrl(uuid+'.'+extension, prefix);

        return AdminConverter.toCreateSignedUri(uuid+"."+extension, url, photoType);
    }

    // 사진 업로드 완료
    @Transactional
    public AdminResDTO.UploadImage uploadImage(
            String fileName,
            PhotoType photoType,
            Long targetId
    ) {

        // 사진 타입에 따라 프리픽스 변경
        String prefix = switch (photoType){
            case PROFILE -> "profile/";
            case ITEM -> "item/";
            case BACKGROUND -> "background/";
            case PET -> "pet/";
        };

        // GCS에서 객체 찾기
        Blob object = gcsUtil.findObject(fileName, prefix);

        if (object == null) {
            throw new AdminException(AdminErrorCode.NOT_UPLOAD_PHOTO);
        }

        // 공개 URL 제작
        String publicUrl = "https://storage.googleapis.com/" +
                object.getBlobId().getBucket() +
                "/"+object.getBlobId().getName();

        // 사진 타입에 따라 행동
        switch (photoType) {
            case PROFILE -> {
                // 프리픽스로 검증
                if (!object.getBlobId().getName().startsWith(prefix)) {
                    throw new AdminException(AdminErrorCode.INVADE_PHOTO_TYPE);
                }

                // 타겟 ID로 조회
                Item item = itemRepository.findById(targetId)
                        .orElseThrow(() -> new AdminException(MemberErrorCode.ACTIVE_PROFILE_NOT_FOUND));

                // 프로필 사진인지 검증
                if (!item.getItemType().equals(ItemType.PROFILE_PHOTO)){
                    throw new AdminException(AdminErrorCode.INVADE_PHOTO_TYPE);
                }

                // 기존 사진 객체 삭제
                if (!item.getImageUrl().isBlank()) {
                    String oldObjectName = item.getImageUrl()
                            .substring(item.getImageUrl().lastIndexOf("/")+1);

                    Blob oldObject = gcsUtil.findObject(oldObjectName, prefix);
                    if (oldObject != null) {
                        oldObject.delete();
                    }
                }

                // 사진 URL 변경
                item.updateImageUrl(publicUrl);
            }

            case ITEM -> {
                // 프리픽스로 검증
                if (!object.getBlobId().getName().startsWith(prefix)) {
                    throw new AdminException(AdminErrorCode.INVADE_PHOTO_TYPE);
                }

                Item item = itemRepository.findById(targetId)
                        .orElseThrow(() -> new AdminException(StoreErrorCode.ITEM_NOT_FOUND));

                // 기존 사진 객체 삭제
                if (!item.getImageUrl().isBlank()) {
                    String oldObjectName = item.getImageUrl()
                            .substring(item.getImageUrl().lastIndexOf("/")+1);
                    Blob oldObject = gcsUtil.findObject(oldObjectName, prefix);

                    if (oldObject != null) {
                        oldObject.delete();
                    }
                }

                item.updateImageUrl(publicUrl);
            }
            case BACKGROUND -> {
                // 프리픽스로 검증
                if (!object.getBlobId().getName().startsWith(prefix)) {
                    throw new AdminException(AdminErrorCode.INVADE_PHOTO_TYPE);
                }

                Item item = itemRepository.findById(targetId)
                        .orElseThrow(() -> new AdminException(StoreErrorCode.ITEM_NOT_FOUND));

                // 해당 아이템 종류가 배경화면인지 (프로필 배경이거나 펫 배경)
                if (!item.getItemType().equals(ItemType.PROFILE_BG) && !item.getItemType().equals(ItemType.PET_BG)) {
                    throw new AdminException(AdminErrorCode.INVADE_PHOTO_TYPE);
                }

                // 기존 사진 객체 삭제
                if (!item.getImageUrl().isBlank()) {
                    String oldObjectName = item.getImageUrl().substring(item.getImageUrl().lastIndexOf("/")+1);
                    Blob oldObject = gcsUtil.findObject(oldObjectName, prefix);
                    if (oldObject != null) {
                        oldObject.delete();
                    }
                }

                item.updateImageUrl(publicUrl);
            }

            case PET -> {
                // 프리픽스로 검증
                if (!object.getBlobId().getName().startsWith(prefix)) {
                    throw new AdminException(AdminErrorCode.INVADE_PHOTO_TYPE);
                }

                PetImage petImage = petImageRepository.findById(targetId)
                        .orElseThrow(() -> new AdminException(MemberErrorCode.PET_NOT_FOUND));

                // 기존 사진 객체 삭제
                if (!petImage.getImageUrl().isBlank()) {
                    String oldObjectName = petImage.getImageUrl().substring(petImage.getImageUrl().lastIndexOf("/")+1);
                    Blob oldObject = gcsUtil.findObject(oldObjectName, prefix);
                    if (oldObject != null) {
                        oldObject.delete();
                    }
                }

                petImage.updateImageUrl(publicUrl);
            }
        }

        return AdminConverter.toUploadImage(object, publicUrl);
    }

    // 아이템 생성
    @Transactional
    public List<AdminResDTO.CreateItem> createItem(
            List<AdminReqDTO.@Valid CreateItem> dto
    ) {
        List<Item> itemList = new ArrayList<>();
        dto.forEach(i -> itemList.add(StoreConverter.toItem(i.name(),i.price(),i.itemType())));

        List<Item> result = itemRepository.saveAll(itemList);

        return result.stream()
                .map(AdminConverter::toCreateItem)
                .toList();
    }
}
