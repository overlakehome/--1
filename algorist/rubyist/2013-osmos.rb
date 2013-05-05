#!/usr/bin/env /usr/local/bin/ruby

%w{test/unit open-uri}.each { |e| require e }

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
    memos = {}
    map = lambda do |k, s|
      k += s.shift until s.empty? || k <= s[0]
      memos[k] ||= {}
      memos[k][s.size] ||= case
      when s.empty?
        0
      when 1 == k
        s.size
      else
        1 + [map.call(k, s[1..-1]), map.call(2*k - 1, s) ].min
      end
    end
    x = map.call(n, motes.dup)
    "Case ##{tc}: #{x}"
  end
end

class TestCases < Test::Unit::TestCase
  def test_main
    test_case_uri = 'https://raw.github.com/henry4j/-/master/algorist/rubyist/osmos-testcases/A-small-attempt0.in'
    open(test_case_uri) { |io| CodeJam.main(io) }
  end
end

# CodeJam.main(STDIN)
