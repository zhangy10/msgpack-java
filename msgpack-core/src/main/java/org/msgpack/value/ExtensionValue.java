//
// MessagePack for Java
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.value;

/**
 * The interface {@code ExtensionValue} represents MessagePack's Extension type.
 * <p/>
 * MessagePack's Extension type can represent represents a tuple of type information and a byte array where type information is an
 * integer whose meaning is defined by applications.
 * <p/>
 * As the type information, applications can use 0 to 127 as the application-specific types. -1 to -128 is reserved for MessagePack's future extension.
 */
public interface ExtensionValue
        extends Value
{
    byte getType();

    byte[] getData();
}
