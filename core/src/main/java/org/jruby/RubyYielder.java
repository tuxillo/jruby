/***** BEGIN LICENSE BLOCK *****
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.jruby;

import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.BlockCallback;
import org.jruby.runtime.CallBlock19;
import org.jruby.runtime.ClassIndex;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.Signature;
import org.jruby.runtime.ThreadContext;
import static org.jruby.runtime.Visibility.*;
import org.jruby.runtime.builtin.IRubyObject;

@JRubyClass(name = "Enumerator::Yielder")
public class RubyYielder extends RubyObject {
    private RubyProc proc; 

    public static RubyClass createYielderClass(Ruby runtime) {
        RubyClass yielderc = runtime.defineClassUnder("Yielder", runtime.getObject(), YIELDER_ALLOCATOR, runtime.getEnumerator());
        runtime.setYielder(yielderc);
        yielderc.setClassIndex(ClassIndex.YIELDER);
        yielderc.kindOf = new RubyModule.JavaClassKindOf(RubyYielder.class);

        yielderc.defineAnnotatedMethods(RubyYielder.class);
        return yielderc;
    }

    private static ObjectAllocator YIELDER_ALLOCATOR = new ObjectAllocator() {
        @Override
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RubyYielder(runtime, klass);
        }
    };

    public RubyYielder(Ruby runtime, RubyClass klass) {
        super(runtime, klass);
    }

    public RubyYielder(Ruby runtime) {
        super(runtime, runtime.getYielder());
    }

    public static RubyYielder newYielder(ThreadContext context, final Block block) {
        Ruby runtime = context.runtime;
        RubyYielder yielder = new RubyYielder(runtime, runtime.getYielder());
        yielder.initialize(context, CallBlock19.newCallClosure(
                yielder,
                yielder.metaClass,
                Signature.NO_ARGUMENTS,
                new BlockCallbackImpl(block),
                context));

        return yielder;
    }

    private static class BlockCallbackImpl implements BlockCallback {

        private final Block block;

        BlockCallbackImpl(Block block) {
            this.block = block;
        }

        public IRubyObject call(ThreadContext context, IRubyObject[] args, Block inner) {
            return block.call(context, args, inner);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject arg, Block inner) {
            return block.call(context, arg, inner);
        }

    }

    private void checkInit() {
        if (proc == null) throw getRuntime().newArgumentError("uninitializer yielder");
    }

    @JRubyMethod(visibility = PRIVATE)
    public IRubyObject initialize(ThreadContext context, Block block) {
        Ruby runtime = context.runtime;
        if (!block.isGiven()) throw runtime.newLocalJumpErrorNoBlock();
        proc = runtime.newProc(Block.Type.PROC, block);
        return this;
    }

    @JRubyMethod(rest = true)
    public IRubyObject yield(ThreadContext context, IRubyObject[] args) {
        checkInit();
        return proc.call(context, args);
    }

    @JRubyMethod(name = "<<", rest = true)
    public IRubyObject op_lshift(ThreadContext context, IRubyObject[] args) {
        yield(context, args);
        return this;
    }
}
