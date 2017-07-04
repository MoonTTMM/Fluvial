package fluvial.model.storage.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created by superttmm on 26/05/2017.
 */
@Service
public class SimpleTransactionService {
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public SimpleTransactionService(PlatformTransactionManager transactionManager){
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void executeTransaction(TransactionService task){
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                task.transactionExecute();
            }
        });
    }
}
