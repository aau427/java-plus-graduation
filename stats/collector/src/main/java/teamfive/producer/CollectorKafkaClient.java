package teamfive.producer;


import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import teamfive.BaseKafkaClient;

@Component
public class CollectorKafkaClient extends BaseKafkaClient {

    //лень - двигатель прогресса!
    public CollectorKafkaClient(@Qualifier("UserActionProducer")
                                KafkaProducer<Long, SpecificRecordBase> producer) {
        super(producer);
    }
}

