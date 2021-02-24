package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.exceptions.NoParameterlessConstructorException;
import main.java.com.nosaiii.sjorm.exceptions.NoSuchPropertyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

public class Query<T extends Model> {
    private List<T> collection;

    public Query(ResultSet resultSet, Class<T> modelClass) throws NoParameterlessConstructorException {
        collection = new ArrayList<>();

        try {
            while (resultSet.next()) {
                Model model = null;
                try {
                    model = modelClass.newInstance();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                //noinspection unchecked
                collection.add((T) model);
            }
        } catch(InstantiationException e) {
            throw new NoParameterlessConstructorException(modelClass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Query(List<T> fromList) {
        collection = fromList;
    }

    private Query(Query<T> base) {
        collection = new ArrayList<>();
        collection.addAll(base.collection);
    }

    public boolean all(Predicate<T> predicate) {
        for (T m : collection) {
            if (!predicate.test(m)) {
                return false;
            }
        }

        return true;
    }

    public boolean any(Predicate<T> predicate) {
        for (T m : collection) {
            if (predicate.test(m)) {
                return true;
            }
        }

        return false;
    }

    public double average(String propertyName) {
        return sum(propertyName) / collection.size();
    }

    public boolean contains(T model) {
        for (T m : collection) {
            if (m == model) {
                return true;
            }
        }

        return false;
    }

    public int count() {
        return collection.size();
    }

    public Query<T> distinct() {
        Query<T> cloned = clone();

        for (T m : collection) {
            if (!cloned.contains(m)) {
                cloned.collection.add(m);
            }
        }

        return cloned;
    }

    public T first() {
        if(collection.isEmpty()) {
            return null;
        }

        return collection.get(0);
    }

    public T firstOrDefault(Predicate<T> predicate) {
        return where(predicate).first();
    }

    public T last() {
        if(collection.isEmpty()) {
            return null;
        }

        return collection.get(collection.size() - 1);
    }

    public <V> Map<V, Query<T>> groupBy(String propertyName) {
        Map<V, Query<T>> groupedMap = new HashMap<>();

        for (T m : collection) {
            V propertyValue = null;
            try {
                //noinspection unchecked
                propertyValue = (V) m.getProperty(propertyName);
            } catch (NoSuchPropertyException e) {
                e.printStackTrace();
            }

            Query<T> query = new Query<>(this);
            if (groupedMap.containsKey(propertyValue)) {
                query = groupedMap.get(propertyValue);
            }

            query.collection.add(m);
            groupedMap.put(propertyValue, query);
        }

        return groupedMap;
    }

    public <V> Query<T> orderBy(String propertyName) {
        Query<T> cloned = clone();

        cloned.collection.sort((o1, o2) -> {
            try {
                Object property1 = o1.getProperty(propertyName), property2 = o2.getProperty(propertyName);

                if (!(property1 instanceof Comparable) || !(property2 instanceof Comparable)) {
                    throw new IllegalArgumentException("Given properties can not be compared");
                }

                @SuppressWarnings("unchecked")
                Comparable<V> propertyComparable = (Comparable<V>) property1;
                //noinspection unchecked
                return propertyComparable.compareTo((V) property2);
            } catch(NoSuchPropertyException e) {
                e.printStackTrace();
            }

            return 0;
        });

        return cloned;
    }

    public Query<T> orderByDescending(String fieldName) {
        return orderBy(fieldName).reverse();
    }

    public Query<T> reverse() {
        Query<T> cloned = clone();
        cloned.collection = new ArrayList<>();

        for (int i = collection.size() - 1; i >= 0; i--) {
            cloned.collection.add(collection.get(i));
        }

        return cloned;
    }

    public double max(String propertyName) {
        if(collection.isEmpty()) {
            return 0;
        }

        double highest = Double.MIN_VALUE;

        for(T m : collection) {
            try {
                double propertyValue = m.getProperty(propertyName, double.class);

                if(propertyValue > highest) {
                    highest = propertyValue;
                }
            } catch(NoSuchPropertyException e) {
                e.printStackTrace();
            }
        }

        return highest;
    }

    public double min(String propertyName) {
        if(collection.isEmpty()) {
            return 0;
        }

        double lowest = Double.MAX_VALUE;

        for(T m : collection) {
            try {
                double propertyValue = m.getProperty(propertyName, double.class);

                if(propertyValue < lowest) {
                    lowest = propertyValue;
                }
            } catch(NoSuchPropertyException e) {
                e.printStackTrace();
            }
        }

        return lowest;
    }

    public <S> List<S> select(String propertyName) {
        List<S> list = new ArrayList<>();

        for(T m : collection) {
            try {
                //noinspection unchecked
                list.add((S) m.getProperty(propertyName));
            } catch (NoSuchPropertyException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public double sum(String propertyName) {
        double sum = 0;

        for(T m : collection) {
            try {
                double propertyValue = m.getProperty(propertyName, double.class);
                sum += propertyValue;
            } catch(NoSuchPropertyException e) {
                e.printStackTrace();
            }
        }

        return sum;
    }

    public Query<T> where(Predicate<T> predicate) {
        Query<T> cloned = clone();

        for(T m : collection) {
            if(!predicate.test(m)) {
                cloned.collection.remove(m);
            }
        }

        return cloned;
    }

    public T[] toArray() {
        //noinspection unchecked
        return (T[]) collection.toArray(new Model[0]);
    }

    public List<T> toList() {
        return collection;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Query<T> clone() {
        return new Query<>(this);
    }
}
