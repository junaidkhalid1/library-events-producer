package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isA;

@ExtendWith(MockitoExtension.class) // ExtendWith Because not the controller class, as in controller class we use WebMvcTest
public class LibraryEventProducerUnitTest {

    @Mock // To inject the dependency
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Spy // To see the real result just like happening in the real class not just mock
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks // Create an instance of the class that is under test and here it's LibraryEventProducer
    LibraryEventProducer producer;

    @Test
    void sendLibraryEvent_Approach3_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Junaid")
                .bookName("Kafka using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        SettableListenableFuture<SendResult<Integer,String>> future = new SettableListenableFuture<>();
        future.setException(new RuntimeException("Exception calling kafka"));

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        //expect
        assertThrows(Exception.class, () -> producer.sendLibraryEvent_Approach3(libraryEvent).get());
    }
}
