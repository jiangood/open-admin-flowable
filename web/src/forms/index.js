import {FormRegistry} from '../framework';
import startFormForm from './start_formForm';
import managerApproveFormForm from './manager_approve_formForm';
import finishViewForm from './finish_viewForm';
import demoForm from './demoForm';

FormRegistry.register('start_formForm', startFormForm);
FormRegistry.register('manager_approve_formForm', managerApproveFormForm);
FormRegistry.register('finish_viewForm', finishViewForm);
FormRegistry.register('demoForm', demoForm);
