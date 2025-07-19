package class1.util;

public class Stack {
    private char[] _elements;
    private int _size;
    private int _actualSize;
    private String _name = "Stack";

    
    public Stack(int capacity) {
        _elements = new char[capacity];
        _size = 0;
        _actualSize = capacity;
    }

    public Stack(int capacity, String name) {
        this(capacity);
        _name = name;
    }

    public void push(char element) {
        if (_size == _elements.length) {
            resize();
        }
        _elements[_size++] = element;
    }

    public char pop() {
        if (_size == 0) {
            throw new IllegalStateException("Stack is empty");
        }
        char element = _elements[_size - 1];
        _size--;
        return element;
    }

    public int size() {
        return _size;
    }

    public int capacity() {
        return _actualSize;
    }

    public boolean isEmpty() {
        return _size == 0;
    }

    private void resize() {
        // resize the array
        char[] newElements = new char[_actualSize * 2];
        
        for (int i = 0; i < _size; i++) {
            newElements[i] = _elements[i];
        }
        
        _actualSize *= 2;
        _elements = newElements;
    }

    @Override
    public String toString() {
        return _name;
    }
}
