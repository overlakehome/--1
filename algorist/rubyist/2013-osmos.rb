#!/usr/bin/env /usr/local/bin/ruby

#%w{test/unit open-uri}.each { |e| require e }

module CodeJam
  def self.main(io)
    cases = 1.upto(io.readline.to_i).map do |tc|
      n, m = io.readline.chomp.split.map { |e| e.to_i }
      m = io.readline.chomp.split.map { |e| e.to_i }
      [tc, n, m.sort]
    end
    cases.each do |c| 
      puts solve(*c)
    end
  end

  def self.solve(tc, n, motes)
    memos = [] # maximum values by k capacity
    map = lambda do |w, s|
      case
      when s.empty?
        0
      when 1 == w
        s.size
      else
        w += s.shift while w > s[0]
        1 + [map.call(w, s[1..-1]), map.call(w, [w-1] + s) ].min
      end
    end
    "Case ##{tc}: #{map.call(n, motes)}"
  end
end

#class TestCases < Test::Unit::TestCase
#  def test_main
#    test_case_uri = 'https://raw.github.com/henry4j/-/master/algorist/rubyist/osmos-testcases/unit-test.in'
#    open(test_case_uri) { |io| CodeJam.main(io) }
#  end
#end

CodeJam.main(STDIN)
