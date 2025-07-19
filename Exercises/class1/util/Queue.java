package class1.util;

public class Queue {
    private char[] _elements;
    private int _size;
    private int _actualSize;

    public Queue(int capacity) {
        _elements = new char[capacity];
        _size = 0;
        _actualSize = capacity;
    }

    public void add(char element) {
        if (_size == _elements.length) {
            resize();
        }
        _elements[_size++] = element;
    }

    public char remove() {
        if (_size == 0) {
            throw new IllegalStateException("Queue is empty");
        }
        char element = _elements[0];
        // remove the element from the array
        for (int i = 0; i < _size - 1; i++) {
            _elements[i] = _elements[i + 1];
        }
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
}