package ch.epfl.sweng.calamar;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Created by pierre on 11/6/15.
 */
public class ConditionTest {

    /**
     * helps defining observers that test Condition
     * TO for TestingObserver
     */
    class TO extends Condition.Observer {

        private int triggered = 0;
        private Condition c;

        TO(Condition c)
        {
            this.c = c;
            this.c.addObserver(this);
        }

        @Override
        public void update()
        {
            triggered++;
        }

        public void assertValue(boolean value)
        {
            assert(value = c.getValue());
        }

        public void assertTrigerred(int n)
        {
            assert(triggered == n);
        }

        public void assAll(boolean value, int n)
        {
            assertValue(value);
            assertTrigerred(n);
        }
    }

    /**
     * used to make setValue accessible
     * TC for TestingCondition
     */
    class TC extends Condition
    {

        TC()
        {
            setValue(false);
        }
        //don't realy care
        @Override
        protected void compose(JSONObject json) throws JSONException {

        }

        void set(boolean value)
        {
            setValue(value);
        }
    }

    @Test
    void testConditionTrueFalse()
    {
        TO o1 = new TO(Condition.trueCondition());
        o1.assAll(true, 0);
        TO o2 = new TO(Condition.falseCondition());
        o2.assAll(false, 0);
    }

    @Test
    void testConditionAnd()
    {
        TC a = new TC(), b = new TC();
        TO o = new TO(Condition.and(a, b));
        o.assAll(false, 0);
        a.set(false);
        b.set(false);
        o.assAll(false, 0);
        a.set(true);
        b.set(true);
        o.assAll(true, 1);
        a.set(false);
        o.assAll(false, 2);
        b.set(false);
        o.assAll(false, 2);
    }

    void testConditionOr()
    {
        TC a = new TC(), b = new TC();
        TO o = new TO(Condition.or(a, b));
        o.assAll(false, 0);
        a.set(false);
        b.set(false);
        o.assAll(false, 0);
        a.set(true);
        o.assAll(true, 1);
        b.set(true);
        o.assAll(true, 1);
        a.set(false);
        b.set(false);
        o.assAll(false, 2);
    }

    void testConditionNot()
    {
        TC a = new TC();
        TO o = new TO(Condition.not(a));
        o.assAll(true, 0);
        a.set(false);
        o.assAll(true, 0);
        a.set(true);
        o.assAll(false, 1);
        a.set(true);
        o.assAll(false, 1);
        a.set(false);
        o.assAll(true, 2);
    }
}
