package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.core.trainservice.system.global.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingRepository embeddingRepository;
    private final ImgRepository imgRepository;

    @Transactional
    public int updateEmbedding(UUID embeddingId, double[] embedding, String calculator) {
        return embeddingRepository.updateEmbedding(embeddingId, embedding, calculator);
    }

    @Transactional
    public <T> T doWithEmbeddingsStream(String apiKey, Function<Stream<Embedding>, T> func) {
        try (Stream<Embedding> stream = embeddingRepository.findBySubjectApiKey(apiKey)) {
            return func.apply(stream);
        }
    }

    public List<Embedding> getWithImgAndCalculatorNotEq(String calculator) {
        return embeddingRepository.getWithImgAndCalculatorNotEq(calculator);
    }

    public Optional<Img> getImg(Embedding embedding) {
        return Optional.ofNullable(embedding.getImg().getId())
                .flatMap(imgRepository::findById);
    }

    public Optional<Img> getImg(String apiKey, UUID embeddingId) {
        return imgRepository.getImgByEmbeddingId(apiKey, embeddingId);
    }

    public Page<EmbeddingProjection> listEmbeddings(String apiKey, Pageable pageable) {
        return embeddingRepository.findBySubjectApiKey(apiKey, pageable);
    }

    public boolean isDemoCollectionInconsistent() {
        return embeddingRepository.countBySubjectApiKeyAndCalculatorNotEq(
                Constants.DEMO_API_KEY,
                Constants.FACENET2018
        ) > 0;
    }

    public boolean isDbInconsistent(String currentCalculator) {
        return embeddingRepository.countBySubjectApiKeyNotEqAndCalculatorNotEq(
                Constants.DEMO_API_KEY,
                currentCalculator
        ) > 0;
    }
}
