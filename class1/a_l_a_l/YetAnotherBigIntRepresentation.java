package class1.a_l_a_l;
public class YetAnotherBigIntRepresentation {
    public String _value;

    public YetAnotherBigIntRepresentation(String value) {
        _value = value;
    }

    @Override
    public String toString() {
        return _value;
    }

    public YetAnotherBigIntRepresentation add(YetAnotherBigIntRepresentation other) {
        // add the two numbers
        String other_value = other.getValue();
        int length1 = _value.length();
        int length2 = other_value.length();
        int carry = 0;

        StringBuilder final_sum = new StringBuilder();
        int n1, n2;

        while (length1 > 0 || length2 > 0) {
            n1 = (length1 > 0) ? _value.charAt(--length1) - '0' : 0;
            n2 = (length2 > 0) ? other_value.charAt(--length2) - '0' : 0;
            int sum = n1 + n2 + carry;
            carry = sum / 10;
            sum = sum % 10;
            final_sum.insert(0, sum);
            // add sum to the result
        }
        return final_sum.toString();
    }

    public YetAnotherBigIntRepresentation multiply(YetAnotherBigIntRepresentation other) {
        // subtract the two numbers
        String other_value = other.getValue();
        int length1 = _value.length(), length2 = other_value.length();
        int carry = 0;
        StringBuilder final_sum = new StringBuilder();
        for (int i = length1 - 1; i >= 0; i--) {
            for (int j = length2 - 1; j >= 0; j--) {
                System.out.println("i: " + i + " j: " + j);
                // multiply the two numbers
                mult = (_value.charAt(i) - '0') * (other_value.charAt(j) - '0') + carry;
                // add the result to the final sum
            }
            int sum = mult + carry;
            carry = sum / 10;
            sum = sum % 10;
            System.out.println("Sum: " + sum + " Carry: " + carry);
        }

        return final_sum.toString();
    }
    
    public String getValue() {
        return _value;
    }

}
