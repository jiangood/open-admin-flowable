import {StringUtils} from "@jiangood/open-admin";


export class ConditionExpressionUtils {

    /**
     * 判断表达式是否是函数
     * @param expr 如 name.contains("aa")
     */
    static isFunction(expr) {
        const regExp = /\w+\.\w+\(.*\)/;
        return regExp.test(expr);
    }

    /**
     * 解析函数, 得到变量，函数名，参数
     * 注意：只支持单参数函数
     * @param expr
     */
    static parseStrFunction(expr) {
        const regExp = /^(\w+)(\.\w+)\("(.*)"\)$/;
        const match = expr.match(regExp);
        if (!match) {
            return null;
        }
        const [, left, op, right] = match;
        return { left, op,right };
    }

    /**
     * 解析普通表达式， 如 a==1,  b>5, c=="你好"
     *
     */
    static parse(expr) {
        // 支持的比较操作符， 注意顺序，先比较的操作符长的
        const operators = ['==', '!=', '<=', '>=', '<', '>'];

        const op = operators.find(op => StringUtils.contains(expr, op))
        if (!op) {
            return null
        }

        const index = expr.indexOf(op);
        const left = expr.substring(0, index).trim();
        let right = expr.substring(index + op.length).trim();
        right = StringUtils.removePrefix(right, '"')
        right = StringUtils.removeSuffix(right, '"')

        return {left, op, right};
    }


}

