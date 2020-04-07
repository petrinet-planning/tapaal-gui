package dk.aau.cs.io

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier



internal class TapnXmlLoaderTest {

    internal class MalformedXML {
        @Test @Disabled
        fun `Malformed XML should throw an exception`() {
            val tapnXmlLoader = TapnXmlLoader();
            Assertions.assertThrows(Exception::class.java) {
                tapnXmlLoader.load("hello".asInpurtStream())
            }
        }
    }

    class Place {

        @Test
        fun `Parse place`() {
            val net = xmlNet(
                """
                    <place displayName="true" id="Start" initialMarking="1" invariant="&lt; inf" name="Start" nameOffsetX="-5" nameOffsetY="35" positionX="135" positionY="30"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            Assertions.assertEquals(1, r.templates().first().guiModel().places.size)

            val place = r.templates().first().guiModel().places[0]
            Assertions.assertEquals("Start", place.name)
            Assertions.assertEquals(135, place.positionX)
            Assertions.assertEquals(30, place.positionY)

            Assertions.assertEquals(-5, place.nameOffsetX)
            Assertions.assertEquals(35, place.nameOffsetY)

        }

        @Test
        //Older version of TAPAAL saved the positionX/Y and nameOffsetX/Y in double format eg. 35.0
        fun `Place positions can be double formatted`(){

            val net = xmlNet(
                """
                    <place displayName="true" id="Start" initialMarking="1" invariant="&lt; inf" name="Start" nameOffsetX="-5.0" nameOffsetY="35.0" positionX="135.0" positionY="30.0"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            val place = r.templates().first().guiModel().places[0]

            Assertions.assertEquals(135, place.positionX)
            Assertions.assertEquals(30, place.positionY)

            Assertions.assertEquals(-5, place.nameOffsetX)
            Assertions.assertEquals(35, place.nameOffsetY)

        }

        @Test
        fun `Empty place`() {
            val net = xmlNet("<place></place>").asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            Assertions.assertThrows(Exception::class.java){
                tapnXmlLoader.load(net)
            }
        }


    }

    class Transition {
        @Test
        fun `Parse Transition`() {
            val net = xmlNet(
                """
                     <transition angle="90" displayName="true" id="T1" infiniteServer="false" name="T1" nameOffsetX="-5" nameOffsetY="35" positionX="360" positionY="300" priority="0" urgent="false"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            Assertions.assertEquals(1, r.templates().first().guiModel().transitions.size)

            val transition = r.templates().first().guiModel().transitions[0]
            Assertions.assertEquals("T1", transition.name)
            Assertions.assertEquals(360, transition.positionX)
            Assertions.assertEquals(300, transition.positionY)

            Assertions.assertEquals(-5, transition.nameOffsetX)
            Assertions.assertEquals(35, transition.nameOffsetY)

        }

        @Test
        //Older version of TAPAAL saved the positionX/Y and nameOffsetX/Y in double format eg. 35.0
        fun `Transiton positions can be double formatted`(){
            val net = xmlNet(
                """
                     <transition angle="90" displayName="true" id="T1" infiniteServer="false" name="T1" nameOffsetX="-5" nameOffsetY="35" positionX="360" positionY="300" priority="0" urgent="false"/>
                """
            ).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader();

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            val transition = r.templates().first().guiModel().transitions[0]
            Assertions.assertEquals(360, transition.positionX)
            Assertions.assertEquals(300, transition.positionY)

            Assertions.assertEquals(-5, transition.nameOffsetX)
            Assertions.assertEquals(35, transition.nameOffsetY)

        }

        @Test
        fun `Empty Transition`() {
            val net = xmlNet("<transition></transition>").asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            Assertions.assertThrows(Exception::class.java){
                tapnXmlLoader.load(net)
            }
        }

        @Test
        fun `if urgent not defined, default value is false`() {
            val net = xmlNet("""
                     <transition angle="0" displayName="true" id="T1" infiniteServer="false" name="T1" nameOffsetX="-5" nameOffsetY="35" positionX="360" positionY="300" priority="0"/>
                """).asInpurtStream()
            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })

            Assertions.assertFalse( r.network().allTemplates().first().getTransitionByName("T1").isUrgent )


        }
    }

    class InputArc {
        @Test
        fun `Parse Input Arc`() {
            val net = xmlNet(
                """
                        <place displayName="true" id="P0" initialMarking="0" invariant="&lt; inf" name="P0" nameOffsetX="0" nameOffsetY="0" positionX="60" positionY="60"/>
                        <transition angle="0" displayName="true" id="T0" infiniteServer="false" name="T0" nameOffsetX="0" nameOffsetY="0" positionX="240" positionY="60" priority="0" urgent="false"/>
                        <arc id="P0 to T0" inscription="[0,inf)" nameOffsetX="0" nameOffsetY="0" source="P0" target="T0" type="timed" weight="1">
                          <arcpath arcPointType="false" id="0" xCoord="87" yCoord="72"/>
                          <arcpath arcPointType="false" id="1" xCoord="246" yCoord="72"/>
                        </arc>
                """
            ).asInpurtStream()

            val tapnXmlLoader = TapnXmlLoader()

            val r = Assertions.assertDoesNotThrow(ThrowingSupplier {
                tapnXmlLoader.load(net)
            })


        }
    }

}

fun  String.asInpurtStream() : java.io.InputStream {
    return java.io.StringBufferInputStream(this)
}

fun xmlNet(s:String) : String {
    return """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <pnml xmlns="http://www.informatik.hu-berlin.de/top/pnml/ptNetb">
                  <net active="true" id="IntroExample" type="P/T net">
                    $s
                  </net>
                 </pnml>
            """.trimIndent()
}