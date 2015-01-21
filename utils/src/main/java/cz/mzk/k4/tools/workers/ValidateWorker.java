package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.validators.SupplementValidator;

/**
 * Created by rumanekm on 20.1.15.
 */
public class ValidateWorker extends UuidWorker {

    private AccessProvider accessProvider;

    //TODO input list with validator objects
    public ValidateWorker(AccessProvider accessProvider) {
        super(false);
        this.accessProvider = accessProvider;
    }

    @Override
    public void run(String uuid) {
        System.out.println(uuid);
        SupplementValidator supplementValidator = new SupplementValidator(accessProvider);

        if (!supplementValidator.validate(uuid)) {
            System.out.printf("validation error");
        }
    }
}
