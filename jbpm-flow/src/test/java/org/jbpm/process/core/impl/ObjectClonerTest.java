/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.core.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jbpm.process.core.impl.ObjectCloner.Config;
import org.junit.Test;

public class ObjectClonerTest {

    private enum Status {
        SINGLE,
        MARRIED,
        DIVORCED,
        WIDOWED
    }

    public static class NamedPerson extends Person {

        private String name;

        public NamedPerson(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(name);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof NamedPerson)) {
                return false;
            }
            NamedPerson other = (NamedPerson) obj;
            return Objects.equals(name, other.name);
        }
    }

    public static class Room {

        private String id;
        private boolean open;

        public Room() {}

        public Room(Room room) {
            this.id = room.id;
            this.open = room.open;
        }

        public Room(String id, boolean open) {
            this.id = id;
            this.open = open;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isOpen() {
            return open;
        }

        public void setOpen(boolean open) {
            this.open = open;
        }

        @Override
        public String toString() {
            return "Room [id=" + id + ", open=" + open + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, open);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Room)) {
                return false;
            }
            Room other = (Room) obj;
            return Objects.equals(id, other.id) && open == other.open;
        }

    }

    public static class Person {

        private Address address;
        private Status status;
        private int age;

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, age, status);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Person)) {
                return false;
            }
            Person other = (Person) obj;
            return Objects.equals(address, other.address) && age == other.age && status == other.status;
        }

        @Override
        public String toString() {
            return "Person [address=" + address + ", status=" + status + ", age=" + age + "]";
        }
    }

    public static class Address {

        private final String street;
        private final int number;

        public Address(String street, int number) {
            this.street = street;
            this.number = number;
        }

        public String getStreet() {
            return street;
        }

        public int getNumber() {
            return number;
        }

        @Override
        public int hashCode() {
            return Objects.hash(number, street);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Address)) {
                return false;
            }
            Address other = (Address) obj;
            return number == other.number && Objects.equals(street, other.street);
        }
    }

    public static class Picture implements Cloneable {

        private String author;
        private byte[] data;

        public Picture(String author, byte[] data) {
            this.author = author;
            this.data = data;
        }

        public Object clone() {
            return new Picture(author, data);
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(data);
            result = prime * result + Objects.hash(author);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Picture)) {
                return false;
            }
            Picture other = (Picture) obj;
            return Objects.equals(author, other.author) && Arrays.equals(data, other.data);
        }
    }

    public static class BigBrother {

        public String name;
        public Collection<Person> lovers;

        @Override
        public int hashCode() {
            return Objects.hash(lovers, name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BigBrother)) {
                return false;
            }
            BigBrother other = (BigBrother) obj;
            return Objects.equals(lovers, other.lovers) && Objects.equals(name, other.name);
        }
    }

    @Test
    public void testInmutable() {
        Config config = new Config().deepCloneCollections(false);
        Object obj = 5;
        assertSame(obj, ObjectCloner.clone(obj));
        obj = true;
        assertSame(obj, ObjectCloner.clone(obj));
        obj = "pepe";
        assertSame(obj, ObjectCloner.clone(obj));
        obj = Arrays.asList("1", "2", "3");
        assertSame(obj, ObjectCloner.clone(obj, config));
        obj = Collections.singletonMap("pepe", "forever");
        assertSame(obj, ObjectCloner.clone(obj, config));
        obj = null;
        assertSame(obj, ObjectCloner.clone(obj));
    }

    @Test
    public void testMutablePOJOWithDefaultConstructor() {
        Person person = new Person();
        person.setAddress(new Address("Rue del Percebe", 13));
        person.setStatus(Status.MARRIED);
        person.setAge(101);
        Object cloned = ObjectCloner.clone(person);
        assertNotSame(person, cloned);
        assertEquals(person, cloned);
    }

    @Test
    public void testPrimitiveArray() {
        int[] object = {2, 3, 5, 7, 11, 13, 17};
        int[] cloned = (int[]) ObjectCloner.clone(object);
        assertNotSame(object, cloned);
        assertArrayEquals(object, cloned);
    }

    @Test
    public void testObjectArray() {
        Object[] object = {new NamedPerson("pepe"), new Room("id", false)};
        Object[] cloned = (Object[]) ObjectCloner.clone(object);
        assertNotSame(object, cloned);
        assertArrayEquals(object, cloned);
    }

    @Test
    public void testCollection() {
        Collection object = new ArrayList<>();
        object.add(new NamedPerson("pepe"));
        object.add(new Room("pepe", false));
        Object cloned = ObjectCloner.clone(object);
        assertNotSame(object, cloned);
        assertEquals(object, cloned);
    }

    @Test
    public void testMap() {
        Map object = new HashMap<>();
        object.put("person", new NamedPerson("pepe"));
        object.put("room", new Room("pepe", false));
        Object cloned = ObjectCloner.clone(object);
        assertNotSame(object, cloned);
        assertEquals(object, cloned);
    }

    @Test
    public void testCloneable() {
        Picture object = new Picture("javierito", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        Object cloned = ObjectCloner.clone(object);
        assertNotSame(object, cloned);
        assertEquals(object, cloned);
    }

    @Test
    public void testMutablePOJOWithConstructor() {
        Person person = new NamedPerson("pepe");
        person.setAddress(new Address("Rue del Percebe", 13));
        person.setStatus(Status.SINGLE);
        person.setAge(23);
        Object cloned = ObjectCloner.clone(person);
        assertNotSame(person, cloned);
        assertEquals(person, cloned);
    }

    @Test
    public void testMutablePOJOWithCopyConstructor() {
        Room room = new Room();
        room.setId("Aloha");
        room.setOpen(true);
        Object cloned = ObjectCloner.clone(room);
        assertNotSame(room, cloned);
        assertEquals(room, cloned);
    }

    @Test
    public void testPojoWithPublicFields() {
        BigBrother object = new BigBrother();
        object.name = "nosecomeunarosca";
        object.lovers = Collections.unmodifiableList(Collections.emptyList());
        Object cloned = ObjectCloner.clone(object);
        assertNotSame(object, cloned);
        assertEquals(object, cloned);
    }

    @Test
    public void testInmutablePOJO() {
        Address address = new Address("Rue del Percebe", 13);
        assertSame(address, ObjectCloner.clone(address));
    }

}
