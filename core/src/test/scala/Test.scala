import weaver.SimpleIOSuite

object Test extends SimpleIOSuite:

  pureTest("a simple pure test"):
    expect(List(1).length == 1)

end Test
